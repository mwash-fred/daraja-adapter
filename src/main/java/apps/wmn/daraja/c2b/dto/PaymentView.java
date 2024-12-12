package apps.wmn.daraja.c2b.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Payment details view")
public record PaymentView(
        @JsonProperty("uuid")
        @Schema(description = "Payment UUID")
        UUID uuid,

        @JsonProperty("transaction_id")
        @Schema(description = "Unique transaction identifier")
        String transactionId,

        @JsonProperty("phone_number")
        @Schema(description = "Customer phone number")
        String phoneNumber,

        @JsonProperty("amount")
        @Schema(description = "Transaction amount")
        BigDecimal amount,

        @JsonProperty("charges_amount")
        @Schema(description = "Transaction charges")
        BigDecimal chargesAmount,

        @JsonProperty("currency")
        @Schema(description = "Transaction currency")
        String currency,

        @JsonProperty("account_reference")
        @Schema(description = "Account reference")
        String accountReference,

        @JsonProperty("transaction_desc")
        @Schema(description = "Transaction description")
        String transactionDesc,

        @JsonProperty("transaction_type")
        @Schema(description = "Type of transaction")
        String transactionType,

        @JsonProperty("transaction_status")
        @Schema(description = "Status of the transaction")
        String transactionStatus,

        @JsonProperty("payer_identifier")
        @Schema(description = "Identifier of the payer")
        String payerIdentifier,

        @JsonProperty("payee_identifier")
        @Schema(description = "Identifier of the payee")
        String payeeIdentifier,

        @JsonProperty("created_date")
        @Schema(description = "Transaction creation date")
        LocalDateTime createdDate,

        @JsonProperty("completed_date")
        @Schema(description = "Transaction completion date")
        LocalDateTime completedDate
) {}