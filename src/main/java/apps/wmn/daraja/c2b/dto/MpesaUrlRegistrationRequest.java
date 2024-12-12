package apps.wmn.daraja.c2b.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MpesaUrlRegistrationRequest(
        @JsonProperty("ShortCode")
        String shortCode,

        @JsonProperty("ResponseType")
        String responseType,

        @JsonProperty("ConfirmationURL")
        String confirmationUrl,

        @JsonProperty("ValidationURL")
        String validationUrl
) {}
