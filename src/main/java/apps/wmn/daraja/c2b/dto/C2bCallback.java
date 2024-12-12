package apps.wmn.daraja.c2b.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "C2B callback payload")
public record C2bCallback(
        @JsonProperty("TransactionType")
        @Schema(description = "Type of transaction", example = "Pay Bill")
        String transactionType,

        @JsonProperty("TransID")
        @Schema(description = "Unique transaction identifier", example = "RKTQDM7W6S")
        String transId,

        @JsonProperty("TransTime")
        @Schema(description = "Transaction timestamp", example = "20191122063845")
        String transTime,

        @JsonProperty("TransAmount")
        @Schema(description = "Transaction amount", example = "10")
        String transAmount,

        @JsonProperty("BusinessShortCode")
        @Schema(description = "Business short code", example = "600638")
        String businessShortCode,

        @JsonProperty("BillRefNumber")
        @Schema(description = "Bill reference number", example = "invoice008")
        String billRefNumber,

        @JsonProperty("InvoiceNumber")
        @Schema(description = "Invoice number", example = "")
        String invoiceNumber,

        @JsonProperty("OrgAccountBalance")
        @Schema(description = "Organization account balance", example = "")
        String orgAccountBalance,

        @JsonProperty("ThirdPartyTransID")
        @Schema(description = "Third party transaction ID", example = "")
        String thirdPartyTransId,

        @JsonProperty("MSISDN")
        @Schema(description = "Customer phone number", example = "25470****149")
        String msisdn,

        @JsonProperty("FirstName")
        @Schema(description = "Customer first name", example = "John")
        String firstName,

        @JsonProperty("MiddleName")
        @Schema(description = "Customer middle name", example = "")
        String middleName,

        @JsonProperty("LastName")
        @Schema(description = "Customer last name", example = "Doe")
        String lastName
) {}