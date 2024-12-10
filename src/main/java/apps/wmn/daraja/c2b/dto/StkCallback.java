package apps.wmn.daraja.c2b.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Callback DTOs
 */
@Schema(description = "STK Push callback payload")
public record StkCallback(
        @JsonProperty("MerchantRequestID")
        String merchantRequestId,

        @JsonProperty("CheckoutRequestID")
        String checkoutRequestId,

        @JsonProperty("ResultCode")
        String resultCode,

        @JsonProperty("ResultDesc")
        String resultDesc,

        CallbackMetadata callbackMetadata
) {
    public record CallbackMetadata(
            @JsonProperty("Item")
            Item[] items
    ) {
        public record Item(
                String name,
                Object value
        ) {}
    }
}
