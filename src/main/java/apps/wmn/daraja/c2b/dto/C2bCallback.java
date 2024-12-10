package apps.wmn.daraja.c2b.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "C2B callback payload")
public record C2bCallback(
        String transactionType,
        String transId,
        String transTime,
        String transAmount,
        String businessShortCode,
        String billRefNumber,
        String invoiceNumber,
        String orgAccountBalance,
        String thirdPartyTransId,
        String msisdn,
        String firstName,
        String middleName,
        String lastName
) {}
