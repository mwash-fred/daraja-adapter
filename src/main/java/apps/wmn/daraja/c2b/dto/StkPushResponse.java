package apps.wmn.daraja.c2b.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTOs */
@Schema(description = "STK Push API response")
public record StkPushResponse(
        @JsonProperty("MerchantRequestID")
        String merchantRequestId,

        @JsonProperty("CheckoutRequestID")
        String checkoutRequestId,

        @JsonProperty("ResponseCode")
        String responseCode,

        @JsonProperty("ResponseDescription")
        String responseDescription,

        @JsonProperty("CustomerMessage")
        String customerMessage
) {}
