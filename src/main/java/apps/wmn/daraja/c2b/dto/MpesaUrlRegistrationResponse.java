package apps.wmn.daraja.c2b.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MpesaUrlRegistrationResponse(
        @JsonProperty("OriginatorCoversationID")
        String originatorConversationId,

        @JsonProperty("ResponseCode")
        String responseCode,

        @JsonProperty("ResponseDescription")
        String responseDescription
) {}
