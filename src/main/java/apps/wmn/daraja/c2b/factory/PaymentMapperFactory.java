package apps.wmn.daraja.c2b.factory;

import apps.wmn.daraja.c2b.dto.*;
import apps.wmn.daraja.c2b.entity.MpesaPayment;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Slf4j
public class PaymentMapperFactory {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private PaymentMapperFactory() {}

    public static MpesaPayment toEntity(StkPushRequest request) {
        return MpesaPayment.builder()
                .phoneNumber(request.phoneNumber())
                .amount(request.amount())
                .accountReference(request.accountReference())
                .transactionDesc(request.transactionDesc())
                .transactionType("STK_PUSH")
                .transactionStatus("PENDING")
                .currency("KES")
                .businessShortCode(request.shortCode())
                .rawRequest(toJson(request))
                .retryCount(0)
                .build();
    }

    public static void updateWithStkResponse(MpesaPayment entity, StkPushResponse response) {
        entity.setTransactionId(response.merchantRequestId());
        entity.setRawCallback(toJson(response));
    }

    public static void updateWithStkCallback(MpesaPayment entity, StkCallback callback) {
        Map<String, Object> metadata = extractStkMetadata(callback.callbackMetadata());

        entity.setTransactionStatus("0".equals(callback.resultCode()) ? "COMPLETED" : "FAILED");
        entity.setCompletedDate(java.time.LocalDateTime.now());
        entity.setMsisdn((String) metadata.get("PhoneNumber"));
        entity.setTransactionId((String) metadata.get("MpesaReceiptNumber"));
        entity.setRawCallback(toJson(callback));

        if (metadata.containsKey("Amount")) {
            entity.setAmount(new java.math.BigDecimal(metadata.get("Amount").toString()));
        }
    }

    public static void updateWithC2BCallback(MpesaPayment entity, C2bCallback callback) {
        log.debug("C2B callback received: {}", callback);
        entity.setTransactionId(callback.transId());
        entity.setMsisdn(callback.msisdn());
        entity.setPhoneNumber(callback.msisdn());
        entity.setCurrency("KES");

        try {
            String amount = callback.transAmount() != null && !callback.transAmount().isBlank() ?
                    callback.transAmount() : "0.0";
            entity.setAmount(new java.math.BigDecimal(amount));
        } catch (NumberFormatException e) {
            log.warn("Invalid amount format in callback: {}", callback.transAmount());
            entity.setAmount(java.math.BigDecimal.ZERO);
        }

        entity.setBillRefNumber(callback.billRefNumber());
        entity.setBusinessShortCode(callback.businessShortCode());

        try {
            String transType = callback.transactionType() != null ?
                    callback.transactionType().toUpperCase().replaceAll("\\s+", "") :
                    "PAYBILL";
            entity.setTransactionType(transType);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid transaction type: {}", callback.transactionType());
            entity.setTransactionType("PAYBILL");
        }

        entity.setTransactionStatus("COMPLETED");

        try {
            // Parse transaction time from format "yyyyMMddHHmmss"
            if (callback.transTime() != null && !callback.transTime().isBlank()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime transactionTime = LocalDateTime.parse(callback.transTime(), formatter);
                entity.setCompletedDate(transactionTime);
            } else {
                entity.setCompletedDate(LocalDateTime.now());
            }
        } catch (DateTimeParseException e) {
            log.warn("Invalid transaction time format: {}", callback.transTime());
            entity.setCompletedDate(LocalDateTime.now());
        }

        try {
            String balance = callback.orgAccountBalance() != null && !callback.orgAccountBalance().isBlank() ?
                    callback.orgAccountBalance() : "0.0";
            entity.setOrgAccountBalance(new java.math.BigDecimal(balance));
        } catch (NumberFormatException e) {
            log.warn("Invalid org account balance format: {}", callback.orgAccountBalance());
            entity.setOrgAccountBalance(java.math.BigDecimal.ZERO);
        }

        entity.setThirdPartyTransId(callback.thirdPartyTransId());
        entity.setFirstName(callback.firstName());
        entity.setMiddleName(callback.middleName());
        entity.setLastName(callback.lastName());
        entity.setRawCallback(toJson(callback));

        log.info("Mpesa payment callback processed successfully {}", entity);
    }

    public static PaymentView toView(MpesaPayment entity) {
        return new PaymentView(
                entity.getUuid(),
                entity.getTransactionId(),
                entity.getPhoneNumber(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getAccountReference(),
                entity.getTransactionDesc(),
                entity.getTransactionType(),
                entity.getTransactionStatus(),
                entity.getCreatedDate(),
                entity.getCompletedDate()
        );
    }

    private static Map<String, Object> extractStkMetadata(StkCallback.CallbackMetadata metadata) {
        return java.util.Arrays.stream(metadata.items())
                .collect(java.util.stream.Collectors.toMap(
                        StkCallback.CallbackMetadata.Item::name,
                        StkCallback.CallbackMetadata.Item::value
                ));
    }

    private static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}