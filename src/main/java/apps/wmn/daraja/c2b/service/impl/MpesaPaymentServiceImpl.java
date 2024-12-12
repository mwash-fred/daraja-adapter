package apps.wmn.daraja.c2b.service.impl;

import apps.wmn.daraja.c2b.dto.*;
import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.entity.MpesaPayment;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import apps.wmn.daraja.c2b.enums.TransactionStatus;
import apps.wmn.daraja.c2b.factory.PaymentMapperFactory;
import apps.wmn.daraja.c2b.repository.MpesaPaymentRepository;
import apps.wmn.daraja.c2b.service.MpesaConfigService;
import apps.wmn.daraja.c2b.service.MpesaPaymentService;
import apps.wmn.daraja.common.config.MpesaUrlConfig;
import apps.wmn.daraja.common.exceptions.PaymentException;
import apps.wmn.daraja.common.exceptions.PaymentNotFoundException;
import apps.wmn.daraja.common.services.DarajaAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpesaPaymentServiceImpl implements MpesaPaymentService {

  private final MpesaPaymentRepository paymentRepository;
  private final DarajaAuthenticationService authService;
  private final MpesaConfigService configService;
  private final MpesaUrlConfig mpesaUrlConfig;
  private final RestTemplate restTemplate;

  @Override
  public StkPushResponse initiateSTKPush(StkPushRequest request, MpesaEnvironment environment) {
    try {
      log.info("Initiating STK push for phone number: {}", request.phoneNumber());

      MpesaConfig config = configService.getConfig(request.shortCode(), environment);
      MpesaConfigService.MpesaCredentials credentials =
          configService.getDecryptedCredentials(config);

      // Create payment record
      MpesaPayment payment = createInitialPayment(request);

      // Prepare and execute STK push request
      HttpEntity<Map<String, Object>> requestEntity =
          prepareStkPushRequest(request, config, credentials, environment);
      StkPushResponse response = executeStkPushRequest(requestEntity, environment);

      // Update and save payment record
      updatePaymentWithResponse(payment, response);

      return response;

    } catch (Exception e) {
      log.error("Error initiating STK push payment", e);
      throw new PaymentException("Failed to initiate STK push", e);
    }
  }

  private MpesaPayment createInitialPayment(StkPushRequest request) {
    MpesaPayment payment = PaymentMapperFactory.toEntity(request);
    payment.setBusinessShortCode(request.shortCode());
    return paymentRepository.save(payment);
  }

  private HttpEntity<Map<String, Object>> prepareStkPushRequest(
      StkPushRequest request,
      MpesaConfig config,
      MpesaConfigService.MpesaCredentials credentials,
      MpesaEnvironment environment) {

    HttpHeaders headers =
        createHeaders(
            credentials.consumerKey(),
            credentials.consumerSecret(),
            request.shortCode(),
            environment);
    Map<String, Object> requestBody = createStkRequestBody(request, config, credentials);
    return new HttpEntity<>(requestBody, headers);
  }

  private StkPushResponse executeStkPushRequest(
      HttpEntity<Map<String, Object>> requestEntity, MpesaEnvironment environment) {
    String stkPushUrl = getStkPushUrl(environment);

    ResponseEntity<StkPushResponse> response =
        restTemplate.exchange(stkPushUrl, HttpMethod.POST, requestEntity, StkPushResponse.class);

    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      return response.getBody();
    }

    throw new PaymentException("Failed to initiate STK push: Invalid response from Mpesa");
  }

  private void updatePaymentWithResponse(MpesaPayment payment, StkPushResponse response) {
    PaymentMapperFactory.updateWithStkResponse(payment, response);
    paymentRepository.save(payment);
  }

  @Override
  @Transactional
  public PaymentView processStkCallback(StkCallback callback) {
    try {
      log.info("Processing STK callback for request ID: {}", callback.merchantRequestId());

      MpesaPayment payment =
          paymentRepository
              .findByTransactionId(callback.merchantRequestId())
              .orElseThrow(() -> new PaymentNotFoundException("Payment not found for callback"));

      PaymentMapperFactory.updateWithStkCallback(payment, callback);
      payment = paymentRepository.save(payment);

      log.info(
          "Successfully processed STK callback for transaction: {}", payment.getTransactionId());
      return PaymentMapperFactory.toView(payment);

    } catch (Exception e) {
      log.error("Error processing STK callback", e);
      throw new PaymentException("Failed to process STK callback", e);
    }
  }

  @Override
  @Transactional
  public PaymentView processC2BCallback(C2bCallback callback) {
    try {
      log.info("Processing C2B callback for transaction: {}", callback.transId());

      MpesaPayment payment =
              paymentRepository
                      .findByTransactionId(callback.transId())
                      .orElseGet(
                              () -> {
                                MpesaPayment newPayment = new MpesaPayment();
                                newPayment.setBusinessShortCode(callback.businessShortCode());
                                return newPayment;
                              });

      PaymentMapperFactory.updateWithC2BCallback(payment, callback);
      payment = paymentRepository.save(payment);

      log.info("Successfully processed C2B callback for transaction: {}", payment.getTransactionId());
      return PaymentMapperFactory.toView(payment);

    } catch (Exception e) {
      log.error("Error processing C2B callback", e);
      throw new PaymentException("Failed to process C2B callback", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentView getPayment(UUID paymentId) {
    return paymentRepository.findByUuid(paymentId)
            .map(PaymentMapperFactory::toView)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentView getPaymentByTransactionId(String transactionId) {
    return paymentRepository.findByTransactionId(transactionId)
            .map(PaymentMapperFactory::toView)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + transactionId));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PaymentView> getPayments(
          String phoneNumber,
          String status,
          LocalDateTime startDate,
          LocalDateTime endDate,
          Pageable pageable) {

    TransactionStatus transactionStatus = status != null ?
            TransactionStatus.valueOf(status.toUpperCase()) : null;

    return paymentRepository.findPayments(
            phoneNumber,
            transactionStatus,
            startDate,
            endDate,
            pageable
    ).map(PaymentMapperFactory::toView);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentView> getPaymentsByPhoneNumber(String phoneNumber) {
    return paymentRepository.findByPhoneNumberOrderByCreatedDateDesc(phoneNumber)
            .stream()
            .map(PaymentMapperFactory::toView)
            .toList();
  }

  @Override
  public PaymentView validatePaymentStatus(String transactionId) {
    try {
      log.info("Validating payment status for transaction: {}", transactionId);

      MpesaPayment payment = paymentRepository.findByTransactionId(transactionId)
              .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + transactionId));

      // Here you would typically make a call to M-Pesa API to check status
      log.info("Payment status: {}", payment.getTransactionStatus());
      return PaymentMapperFactory.toView(payment);

    } catch (Exception e) {
      log.error("Error validating payment status", e);
      throw new PaymentException("Failed to validate payment status", e);
    }
  }

  private HttpHeaders createHeaders(
      String consumerKey, String consumerSecret, String shortCode, MpesaEnvironment environment) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(
        authService.getAccessToken(consumerKey, consumerSecret, shortCode, environment));
    return headers;
  }

  private Map<String, Object> createStkRequestBody(
      StkPushRequest request, MpesaConfig config, MpesaConfigService.MpesaCredentials credentials) {

    String timestamp = generateTimestamp();
    String password = generatePassword(config, credentials, timestamp);

    Map<String, Object> body = new HashMap<>();
    body.put("BusinessShortCode", config.getShortcode());
    body.put("Password", password);
    body.put("Timestamp", timestamp);
    body.put("TransactionType", "CustomerPayBillOnline");
    body.put("Amount", request.amount());
    body.put("PartyA", request.phoneNumber());
    body.put("PartyB", config.getShortcode());
    body.put("PhoneNumber", request.phoneNumber());
    body.put("CallBackURL", config.getStkCallbackUrl());
    body.put("AccountReference", request.accountReference());
    body.put("TransactionDesc", request.transactionDesc());

    return body;
  }

  private String generateTimestamp() {
    return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
  }

  private String generatePassword(
      MpesaConfig config, MpesaConfigService.MpesaCredentials credentials, String timestamp) {
    return Base64.getEncoder()
        .encodeToString((config.getShortcode() + credentials.passkey() + timestamp).getBytes());
  }

  private String getStkPushUrl(MpesaEnvironment environment) {
    return MpesaEnvironment.PRODUCTION == environment
        ? mpesaUrlConfig.getUrls().getProd().getStkPushUrl()
        : mpesaUrlConfig.getUrls().getSandbox().getStkPushUrl();
  }
}
