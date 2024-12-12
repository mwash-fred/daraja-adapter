package apps.wmn.daraja.c2b.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Data Transfer Object for STK Push payment requests. Used to initiate M-Pesa STK Push prompts to
 * customers' phones.
 */
@Schema(
    description = "STK Push payment request details",
    name = "StkPushRequest",
    title = "STK Push Request")
public record StkPushRequest(
    @Schema(
            description = "Customer's phone number in international format starting with 254",
            example = "254712345678",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "^254\\d{9}$",
            minLength = 12,
            maxLength = 12)
        @NotBlank(message = "Phone number is required")
        @Pattern(
            regexp = "^254\\d{9}$",
            message = "Phone number must be in format 254XXXXXXXXX where X is a digit")
        @JsonProperty("phone_number")
        String phoneNumber,
    @Schema(
            description = "Amount to be paid in KES (Kenya Shillings). Must be greater than 0",
            example = "100.00",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1")
        @Positive(message = "Amount must be greater than 0")
        @JsonProperty("amount")
        BigDecimal amount,
    @Schema(
            description =
                "Account reference or number for the transaction. Used to identify the payment",
            example = "ABC123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 20)
        @NotBlank(message = "Account reference is required")
        @Size(max = 20, message = "Account reference cannot exceed 20 characters")
        @JsonProperty("account_number")
        String accountReference,
    @Schema(
            description = "Description of the transaction that appears on the STK prompt",
            example = "Payment for service X",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            maxLength = 100)
        @Size(max = 100, message = "Transaction description cannot exceed 100 characters")
        @JsonProperty("transaction_desc")
        String transactionDesc,
    @Schema(
            description = "M-Pesa shortcode (Paybill/Till number) receiving the payment",
            example = "174379",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "^\\d{5,7}$")
        @Pattern(regexp = "^\\d{5,7}$", message = "Shortcode must be 5-7 digits")
        @JsonProperty("short_code")
        @NotBlank(message = "Shortcode is required")
        String shortCode,

        @Schema(
            description = "M-Pesa environment (SANDBOX/PRODUCTION)",
            example = "SANDBOX",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "SANDBOX|PRODUCTION")
        @Pattern(regexp = "SANDBOX|PRODUCTION", message = "Environment must be SANDBOX or PRODUCTION")
        @JsonProperty("environment")
        @NotBlank(message = "Environment is required")
        String environment) {}
