package apps.wmn.daraja.c2b.dto;

import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mpesa configuration creation/update request")
public record CreateMpesaConfigRequest(
        @Schema(description = "Mpesa shortcode", required = true)
        String shortcode,

        @Schema(description = "Environment (SANDBOX/PRODUCTION)", required = true)
        MpesaEnvironment environment,

        @Schema(description = "Consumer key (will be encrypted)", required = true)
        String consumerKey,

        @Schema(description = "Consumer secret (will be encrypted)", required = true)
        String consumerSecret,

        @Schema(description = "Passkey (will be encrypted)")
        String passkey,

        @Schema(description = "Callback URL", required = true)
        String callbackUrl,

        @Schema(description = "Timeout URL", required = true)
        String timeoutUrl,

        @Schema(description = "Result URL", required = true)
        String resultUrl,

        @Schema(description = "STK Push callback URL")
        String stkCallbackUrl,

        @Schema(description = "Configuration description")
        String description
) {
    public MpesaConfig toEntity() {
        MpesaConfig config = new MpesaConfig();
        config.setShortcode(shortcode);
        config.setEnvironment(environment);
        config.setConsumerKey(consumerKey);
        config.setConsumerSecret(consumerSecret);
        config.setPasskey(passkey);
        config.setCallbackUrl(callbackUrl);
        config.setTimeoutUrl(timeoutUrl);
        config.setResultUrl(resultUrl);
        config.setStkCallbackUrl(stkCallbackUrl);
        config.setDescription(description);
        config.setActive(true);
        return config;
    }
}
