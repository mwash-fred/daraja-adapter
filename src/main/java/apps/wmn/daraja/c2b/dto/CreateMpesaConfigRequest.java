package apps.wmn.daraja.c2b.dto;

import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import apps.wmn.daraja.common.enums.ShortcodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

@Schema(description = "Payment configuration creation/update request")
public record CreateMpesaConfigRequest(
        @NotBlank
        @Size(max = 10)
        @Schema(description = "Shortcode", required = true)
        @JsonProperty("shortcode")
        String shortcode,

        @NotNull
        @Schema(description = "Environment (SANDBOX/PRODUCTION)", required = true)
        @JsonProperty("environment")
        MpesaEnvironment environment,

        @NotNull
        @Schema(description = "Shortcode type (COLLECTION/DISBURSEMENT/BOTH)", required = true)
        @JsonProperty("shortcodeType")
        ShortcodeType shortcodeType,

        @NotBlank
        @Size(max = 100)
        @Schema(description = "Consumer key", required = true)
        @JsonProperty("consumerKey")
        String consumerKey,

        @NotBlank
        @Size(max = 100)
        @Schema(description = "Consumer secret", required = true)
        @JsonProperty("consumerSecret")
        String consumerSecret,

        @Size(max = 100)
        @Schema(description = "Passkey")
        @JsonProperty("passkey")
        String passkey,

        @Size(max = 100)
        @Schema(description = "Initiator name for B2C transactions")
        @JsonProperty("initiatorName")
        String initiatorName,

        @Size(max = 255)
        @Schema(description = "Security credential for B2C transactions")
        @JsonProperty("securityCredential")
        String securityCredential,

        @Size(max = 255)
        @Schema(description = "Collection callback URL")
        @JsonProperty("collectionCallbackUrl")
        String collectionCallbackUrl,

        @Size(max = 255)
        @Schema(description = "Collection validation URL")
        @JsonProperty("collectionValidationUrl")
        String collectionValidationUrl,

        @Size(max = 255)
        @Schema(description = "Collection timeout URL")
        @JsonProperty("collectionTimeoutUrl")
        String collectionTimeoutUrl,

        @Size(max = 255)
        @Schema(description = "Collection result URL")
        @JsonProperty("collectionResultUrl")
        String collectionResultUrl,

        @Size(max = 255)
        @Schema(description = "Disbursement result URL")
        @JsonProperty("disbursementResultUrl")
        String disbursementResultUrl,

        @Size(max = 255)
        @Schema(description = "Disbursement timeout URL")
        @JsonProperty("disbursementTimeoutUrl")
        String disbursementTimeoutUrl,

        @Size(max = 255)
        @Schema(description = "Disbursement queue timeout URL")
        @JsonProperty("disbursementQueueUrl")
        String disbursementQueueUrl,

        @Size(max = 255)
        @Schema(description = "STK Push callback URL")
        @JsonProperty("stkCallbackUrl")
        String stkCallbackUrl,

        @Schema(description = "Configuration description")
        @JsonProperty("description")
        String description
) {
    public MpesaConfig toEntity() {
        MpesaConfig config = new MpesaConfig();
        config.setShortcode(shortcode);
        config.setEnvironment(environment);
        config.setShortcodeType(shortcodeType);
        config.setConsumerKey(consumerKey);
        config.setConsumerSecret(consumerSecret);
        config.setPasskey(passkey);
        config.setInitiatorName(initiatorName);
        config.setSecurityCredential(securityCredential);
        config.setCollectionCallbackUrl(collectionCallbackUrl);
        config.setCollectionValidationUrl(collectionValidationUrl);
        config.setCollectionTimeoutUrl(collectionTimeoutUrl);
        config.setCollectionResultUrl(collectionResultUrl);
        config.setDisbursementResultUrl(disbursementResultUrl);
        config.setDisbursementTimeoutUrl(disbursementTimeoutUrl);
        config.setDisbursementQueueUrl(disbursementQueueUrl);
        config.setStkCallbackUrl(stkCallbackUrl);
        config.setDescription(description);
        config.setActive(true);

        return config;
    }
}