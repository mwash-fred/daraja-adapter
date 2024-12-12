package apps.wmn.daraja.c2b.service.impl;

import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import apps.wmn.daraja.c2b.repository.MpesaConfigRepository;
import apps.wmn.daraja.c2b.service.MpesaConfigService;
import apps.wmn.daraja.common.enums.ShortcodeType;
import apps.wmn.daraja.common.event.CallbackValidationUrlRegistrationEvent;
import apps.wmn.daraja.common.exceptions.ConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaConfigImpl implements MpesaConfigService {
    private final MpesaConfigRepository configRepository;
    private final TextEncryptor textEncryptor;
    private final ApplicationEventPublisher eventPublisher;
    private static final String CONFIG_CACHE = "shortcodeConfigs";
    private static final String CONFIG_NOT_FOUND = "Configuration not found";
    private static final String CONFIG_EXISTS = "Configuration already exists for this shortcode and environment";
    private static final String MISSING_COLLECTION_URLS = "Collection URLs are required for COLLECTION or BOTH shortcode types";
    private static final String MISSING_DISBURSEMENT_URLS = "Disbursement URLs are required for DISBURSEMENT or BOTH shortcode types";
    private static final String MISSING_B2C_CREDENTIALS = "Initiator name and security credential are required for DISBURSEMENT or BOTH shortcode types";

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CONFIG_CACHE, key = "#shortcode + '_' + #environment")
    public MpesaConfig getConfig(String shortcode, MpesaEnvironment environment) {
        log.debug("Fetching configuration for shortcode: {} in environment: {}", shortcode, environment);
        return findActiveConfig(shortcode, environment);
    }

    @Override
    @Transactional
    @CacheEvict(value = CONFIG_CACHE, allEntries = true)
    public MpesaConfig createConfig(MpesaConfig config) {
        log.info("Creating new configuration for shortcode: {} in environment: {}", config.getShortcode(), config.getEnvironment());
        validateNewConfig(config);
        validateConfigurationUrls(config);
        MpesaConfig preparedConfig = prepareConfigForSave(config);
        MpesaConfig savedConfig = configRepository.save(preparedConfig);
        publishValidationEvents(savedConfig);
        return savedConfig;
    }

    @Override
    @Transactional
    @CacheEvict(value = CONFIG_CACHE, allEntries = true)
    public MpesaConfig updateConfig(UUID id, MpesaConfig updatedConfig) {
        log.info("Updating configuration with ID: {}", id);
        MpesaConfig existingConfig = findConfigById(id);
        validateConfigurationUrls(updatedConfig);
        updateConfigFields(existingConfig, updatedConfig);
        return configRepository.save(existingConfig);
    }

    @Override
    @Transactional
    @CacheEvict(value = CONFIG_CACHE, allEntries = true)
    public void deactivateConfig(UUID id) {
        log.info("Deactivating configuration with ID: {}", id);
        MpesaConfig config = findConfigById(id);
        config.setActive(false);
        configRepository.save(config);
        log.info("Successfully deactivated configuration for shortcode: {} in environment: {}", config.getShortcode(), config.getEnvironment());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MpesaConfig> listActiveConfigs(Pageable pageable) {
        log.debug("Fetching all active configurations");
        return configRepository.findByActiveTrue(pageable);
    }

    @Override
    public MpesaCredentials getDecryptedCredentials(MpesaConfig config) {
        log.debug("Decrypting credentials for shortcode: {}", config.getShortcode());
        return new MpesaCredentials(
                decryptValue(config.getConsumerKey()),
                decryptValue(config.getConsumerSecret()),
                config.getPasskey() != null ? decryptValue(config.getPasskey()) : null,
                config.getSecurityCredential() != null ? decryptValue(config.getSecurityCredential()) : null
        );
    }

    private MpesaConfig findActiveConfig(String shortcode, MpesaEnvironment environment) {
        return configRepository
                .findByShortcodeAndEnvironmentAndActiveTrue(shortcode, environment)
                .orElseThrow(() -> new ConfigurationException(
                        String.format("No active configuration found for shortcode: %s in environment: %s",
                                shortcode, environment)));
    }

    private MpesaConfig findConfigById(UUID id) {
        return configRepository.findByUuid(id)
                .orElseThrow(() -> new ConfigurationException(CONFIG_NOT_FOUND));
    }

    private void validateNewConfig(MpesaConfig config) {
        if (configRepository.existsByShortcodeAndEnvironment(config.getShortcode(), config.getEnvironment())) {
            throw new ConfigurationException(CONFIG_EXISTS);
        }
    }

    private void validateConfigurationUrls(MpesaConfig config) {
        if (isCollectionConfig(config)) {
            validateCollectionUrls(config);
        }
        if (isDisbursementConfig(config)) {
            validateDisbursementUrls(config);
        }
    }

    private boolean isCollectionConfig(MpesaConfig config) {
        return config.getShortcodeType() == ShortcodeType.COLLECTION || config.getShortcodeType() == ShortcodeType.BOTH;
    }

    private boolean isDisbursementConfig(MpesaConfig config) {
        return config.getShortcodeType() == ShortcodeType.DISBURSEMENT || config.getShortcodeType() == ShortcodeType.BOTH;
    }

    private void validateCollectionUrls(MpesaConfig config) {
        if (config.getCollectionCallbackUrl() == null || config.getCollectionValidationUrl() == null ||
                config.getCollectionTimeoutUrl() == null || config.getCollectionResultUrl() == null) {
            throw new ConfigurationException(MISSING_COLLECTION_URLS);
        }
    }

    private void validateDisbursementUrls(MpesaConfig config) {
        if (config.getDisbursementResultUrl() == null || config.getDisbursementTimeoutUrl() == null) {
            throw new ConfigurationException(MISSING_DISBURSEMENT_URLS);
        }
        if (config.getInitiatorName() == null || config.getSecurityCredential() == null) {
            throw new ConfigurationException(MISSING_B2C_CREDENTIALS);
        }
    }

    private MpesaConfig prepareConfigForSave(MpesaConfig config) {
        MpesaConfig preparedConfig = new MpesaConfig();
        preparedConfig.setShortcode(config.getShortcode());
        preparedConfig.setEnvironment(config.getEnvironment());
        preparedConfig.setShortcodeType(config.getShortcodeType());
        preparedConfig.setConsumerKey(encryptValue(config.getConsumerKey()));
        preparedConfig.setConsumerSecret(encryptValue(config.getConsumerSecret()));
        if (config.getPasskey() != null) {
            preparedConfig.setPasskey(encryptValue(config.getPasskey()));
        }
        if (config.getSecurityCredential() != null) {
            preparedConfig.setSecurityCredential(encryptValue(config.getSecurityCredential()));
        }
        copyNonSensitiveFields(preparedConfig, config);
        return preparedConfig;
    }

    private void updateEncryptedFields(MpesaConfig existingConfig, MpesaConfig updatedConfig) {
        if (updatedConfig.getConsumerKey() != null) {
            existingConfig.setConsumerKey(encryptValue(updatedConfig.getConsumerKey()));
        }
        if (updatedConfig.getConsumerSecret() != null) {
            existingConfig.setConsumerSecret(encryptValue(updatedConfig.getConsumerSecret()));
        }
        if (updatedConfig.getPasskey() != null) {
            existingConfig.setPasskey(encryptValue(updatedConfig.getPasskey()));
        }
        if (updatedConfig.getSecurityCredential() != null) {
            existingConfig.setSecurityCredential(encryptValue(updatedConfig.getSecurityCredential()));
        }
    }

    private void updateConfigFields(MpesaConfig existingConfig, MpesaConfig updatedConfig) {
        updateEncryptedFields(existingConfig, updatedConfig);
        copyNonSensitiveFields(existingConfig, updatedConfig);
    }

    private void copyNonSensitiveFields(MpesaConfig target, MpesaConfig source) {
        target.setCollectionCallbackUrl(source.getCollectionCallbackUrl());
        target.setCollectionValidationUrl(source.getCollectionValidationUrl());
        target.setCollectionTimeoutUrl(source.getCollectionTimeoutUrl());
        target.setCollectionResultUrl(source.getCollectionResultUrl());
        target.setDisbursementResultUrl(source.getDisbursementResultUrl());
        target.setDisbursementTimeoutUrl(source.getDisbursementTimeoutUrl());
        target.setDisbursementQueueUrl(source.getDisbursementQueueUrl());
        target.setInitiatorName(source.getInitiatorName());
        target.setStkCallbackUrl(source.getStkCallbackUrl());
        target.setActive(source.isActive());
        target.setDescription(source.getDescription());
    }

    private void publishValidationEvents(MpesaConfig config) {
        if (isCollectionConfig(config)) {
            eventPublisher.publishEvent(new CallbackValidationUrlRegistrationEvent(
                    config.getShortcode(),
                    config.getEnvironment(),
                    config.getCollectionCallbackUrl(),
                    config.getCollectionValidationUrl()));
        }
    }

    private String encryptValue(String value) {
        return textEncryptor.encrypt(value);
    }

    private String decryptValue(String value) {
        return textEncryptor.decrypt(value);
    }
}