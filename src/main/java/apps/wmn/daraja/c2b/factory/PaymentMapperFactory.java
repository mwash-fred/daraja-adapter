package apps.wmn.daraja.c2b.factory;

import apps.wmn.daraja.c2b.dto.*;
import apps.wmn.daraja.c2b.entity.MpesaPayment;
import apps.wmn.daraja.c2b.enums.TransactionStatus;
import apps.wmn.daraja.c2b.enums.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Factory class for converting between Payment entities and DTOs
 */
public class PaymentMapperFactory {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private PaymentMapperFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a new MpesaPayment entity from STK Push request
     */
    public static MpesaPayment toEntity(StkPushRequest request) {
        return MpesaPayment.builder()
                .phoneNumber(request.phoneNumber())
                .amount(request.amount())
                .accountReference(request.accountReference())
                .transactionDesc(request.transactionDesc())
                .transactionType(TransactionType.STK_PUSH)
                .transactionStatus(TransactionStatus.PENDING)
                .rawRequest(toJson(request))
                .build();
    }

    /**
     * Updates entity with STK Push response
     */
    public static void updateWithStkResponse(MpesaPayment entity, StkPushResponse response) {
        entity.setTransactionId(response.merchantRequestId());
        entity.setRawCallback(toJson(response));
    }

    /**
     * Updates entity with STK callback data
     */
    public static void updateWithStkCallback(MpesaPayment entity, StkCallback callback) {
        Map<String, Object> metadata = extractStkMetadata(callback.callbackMetadata());

        entity.setTransactionStatus("0".equals(callback.resultCode())
                ? TransactionStatus.COMPLETED
                : TransactionStatus.FAILED);
        entity.setCompletedDate(java.time.LocalDateTime.now());
        entity.setMsisdn((String) metadata.get("PhoneNumber"));
        entity.setTransactionId((String) metadata.get("MpesaReceiptNumber"));
        entity.setRawCallback(toJson(callback));

        if (metadata.containsKey("Amount")) {
            entity.setAmount(new java.math.BigDecimal(metadata.get("Amount").toString()));
        }
    }

    /**
     * Updates entity with C2B callback data
     */
    public static void updateWithC2BCallback(MpesaPayment entity, C2bCallback callback) {
        entity.setTransactionId(callback.transId());
        entity.setMsisdn(callback.msisdn());
        entity.setAmount(new java.math.BigDecimal(callback.transAmount()));
        entity.setBillRefNumber(callback.billRefNumber());
        entity.setTransactionType(TransactionType.valueOf(callback.transactionType()));
        entity.setTransactionStatus(TransactionStatus.COMPLETED);
        entity.setCompletedDate(java.time.LocalDateTime.now());
        entity.setBillRefNumber(callback.billRefNumber());
        entity.setOrgAccountBalance(new java.math.BigDecimal(callback.orgAccountBalance()));
        entity.setThirdPartyTransId(callback.thirdPartyTransId());
        entity.setFirstName(callback.firstName());
        entity.setMiddleName(callback.middleName());
        entity.setLastName(callback.lastName());
        entity.setRawCallback(toJson(callback));
    }

    /**
     * Converts entity to view DTO
     */
    public static PaymentView toView(MpesaPayment entity) {
        return new PaymentView(
                entity.getUuid(),
                entity.getTransactionId(),
                entity.getPhoneNumber(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getAccountReference(),
                entity.getTransactionDesc(),
                entity.getTransactionType().name(),
                entity.getTransactionStatus().name(),
                entity.getCreatedDate(),
                entity.getCompletedDate()
        );
    }

    /**
     * Extracts metadata from STK callback
     */
    private static Map<String, Object> extractStkMetadata(StkCallback.CallbackMetadata metadata) {
        return java.util.Arrays.stream(metadata.items())
                .collect(java.util.stream.Collectors.toMap(
                        StkCallback.CallbackMetadata.Item::name,
                        StkCallback.CallbackMetadata.Item::value
                ));
    }

    /**
     * Converts object to JSON string
     */
    private static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}
