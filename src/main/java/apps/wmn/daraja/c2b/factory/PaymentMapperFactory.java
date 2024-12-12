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
                .payerIdentifier(request.phoneNumber())
                .amount(request.amount())
                .accountReference(request.accountReference())
                .transactionDesc(request.transactionDesc())
                .transactionType("STK_PUSH")
                .transactionStatus("PENDING")
                .currency("KES")
                .businessShortCode(request.shortCode())
                .payeeIdentifier(request.shortCode())
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
        entity.setCompletedDate(LocalDateTime.now());
        entity.setMsisdn((String) metadata.get("PhoneNumber"));
        entity.setPhoneNumber((String) metadata.get("PhoneNumber"));
        entity.setPayerIdentifier((String) metadata.get("PhoneNumber"));
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
        entity.setPayerIdentifier(callback.msisdn());
        entity.setPayeeIdentifier(callback.businessShortCode());
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

        String transType = determineTransactionType(callback.transactionType());
        entity.setTransactionType(transType);
        entity.setTransactionStatus("COMPLETED");
        setCompletedDate(entity, callback.transTime());
        setOrgAccountBalance(entity, callback.orgAccountBalance());

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
                entity.getChargesAmount(),
                entity.getCurrency(),
                entity.getAccountReference(),
                entity.getTransactionDesc(),
                entity.getTransactionType(),
                entity.getTransactionStatus(),
                entity.getPayerIdentifier(),
                entity.getPayeeIdentifier(),
                entity.getCreatedDate(),
                entity.getCompletedDate()
        );
    }

    private static String determineTransactionType(String rawType) {
        try {
            if (rawType == null || rawType.isBlank()) {
                return "PAYBILL_COLLECTION";
            }
            String processedType = rawType.toUpperCase().replaceAll("\\s+", "");
            if (processedType.contains("BUYGOODS")) {
                return "BUY_GOODS_COLLECTION";
            }
            return "PAYBILL_COLLECTION";
        } catch (IllegalArgumentException e) {
            log.warn("Invalid transaction type: {}", rawType);
            return "PAYBILL_COLLECTION";
        }
    }

    private static void setCompletedDate(MpesaPayment entity, String transTime) {
        try {
            if (transTime != null && !transTime.isBlank()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime transactionTime = LocalDateTime.parse(transTime, formatter);
                entity.setCompletedDate(transactionTime);
            } else {
                entity.setCompletedDate(LocalDateTime.now());
            }
        } catch (DateTimeParseException e) {
            log.warn("Invalid transaction time format: {}", transTime);
            entity.setCompletedDate(LocalDateTime.now());
        }
    }

    private static void setOrgAccountBalance(MpesaPayment entity, String balance) {
        try {
            String balanceValue = balance != null && !balance.isBlank() ? balance : "0.0";
            entity.setOrgAccountBalance(new java.math.BigDecimal(balanceValue));
        } catch (NumberFormatException e) {
            log.warn("Invalid org account balance format: {}", balance);
            entity.setOrgAccountBalance(java.math.BigDecimal.ZERO);
        }
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