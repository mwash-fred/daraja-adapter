package apps.wmn.daraja.c2b.dto;

import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import apps.wmn.daraja.common.enums.ShortcodeType;
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

        @Schema(description = "Shortcode type (COLLECTION/DISBURSEMENT/BOTH)")
        ShortcodeType shortcodeType,

        @Schema(description = "Initiator name for B2C transactions")
        String initiatorName,

        // Collection URLs
        @Schema(description = "Collection callback URL")
        String collectionCallbackUrl,

        @Schema(description = "Collection validation URL")
        String collectionValidationUrl,

        @Schema(description = "Collection timeout URL")
        String collectionTimeoutUrl,

        @Schema(description = "Collection result URL")
        String collectionResultUrl,

        // Disbursement URLs
        @Schema(description = "Disbursement result URL")
        String disbursementResultUrl,

        @Schema(description = "Disbursement timeout URL")
        String disbursementTimeoutUrl,

        @Schema(description = "Disbursement queue timeout URL")
        String disbursementQueueUrl,

        // STK Push URL
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
                config.getShortcodeType(),
                config.getInitiatorName(),
                config.getCollectionCallbackUrl(),
                config.getCollectionValidationUrl(),
                config.getCollectionTimeoutUrl(),
                config.getCollectionResultUrl(),
                config.getDisbursementResultUrl(),
                config.getDisbursementTimeoutUrl(),
                config.getDisbursementQueueUrl(),
                config.getStkCallbackUrl(),
                config.isActive(),
                config.getDescription()
        );
    }
}