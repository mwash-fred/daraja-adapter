package apps.wmn.daraja.c2b.dto;

import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Mpesa configuration response")
public record MpesaConfigResponse(
        @Schema(description = "Configuration UUID")
        UUID uuid,

        @Schema(description = "Mpesa shortcode")
        String shortcode,

        @Schema(description = "Environment (SANDBOX/PRODUCTION)")
        MpesaEnvironment environment,

        @Schema(description = "Callback URL for transaction notifications")
        String callbackUrl,

        @Schema(description = "Timeout URL for failed transactions")
        String timeoutUrl,

        @Schema(description = "Result URL for transaction results")
        String resultUrl,

        @Schema(description = "STK Push callback URL")
        String stkCallbackUrl,

        @Schema(description = "Configuration status")
        boolean active,

        @Schema(description = "Configuration description")
        String description
) {
    public static MpesaConfigResponse from(MpesaConfig config) {
        return new MpesaConfigResponse(
                config.getUuid(),
                config.getShortcode(),
                config.getEnvironment(),
                config.getCallbackUrl(),
                config.getTimeoutUrl(),
                config.getResultUrl(),
                config.getStkCallbackUrl(),
                config.isActive(),
                config.getDescription()
        );
    }
}
