package apps.wmn.daraja.c2b.service.impl;

import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import apps.wmn.daraja.c2b.repository.MpesaConfigRepository;
import apps.wmn.daraja.c2b.service.MpesaConfigService;
import apps.wmn.daraja.common.exceptions.ConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor @Slf4j
public class MpesConfigImpl implements MpesaConfigService {
    private final MpesaConfigRepository configRepository;
    private final TextEncryptor textEncryptor;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "shortcodeConfigs", key = "#shortcode + '_' + #environment")
    public MpesaConfig getConfig(String shortcode, MpesaEnvironment environment) {
        log.debug("Fetching configuration for shortcode: {} in environment: {}", shortcode, environment);
    return configRepository
        .findByShortcodeAndEnvironmentAndActiveTrue(shortcode, environment)
        .orElseThrow(
            () ->
                new ConfigurationException(
                    "No active configuration found for shortcode: "
                        + shortcode
                        + " in environment: "
                        + environment));
    }

    @Override
    @CacheEvict(value = "shortcodeConfigs", allEntries = true)
    public MpesaConfig createConfig(MpesaConfig config) {
        log.info("Creating new configuration for shortcode: {} in environment: {}",
                config.getShortcode(), config.getEnvironment());

        validateNewConfig(config);
        encryptSensitiveData(config);

        return configRepository.save(config);
    }

    @Override
    @CacheEvict(value = "shortcodeConfigs", allEntries = true)
    public MpesaConfig updateConfig(UUID id, MpesaConfig updatedConfig) {
        log.info("Updating configuration with ID: {}", id);

        MpesaConfig existingConfig = configRepository.findByUuid(id)
                .orElseThrow(() -> new ConfigurationException("Configuration not found"));

        updateConfigFields(existingConfig, updatedConfig);

        return configRepository.save(existingConfig);
    }

    @Override
    @CacheEvict(value = "shortcodeConfigs", allEntries = true)
    public void deactivateConfig(UUID id) {
        log.info("Deactivating configuration with ID: {}", id);

        MpesaConfig config = configRepository.findByUuid(id)
                .orElseThrow(() -> new ConfigurationException("Configuration not found"));

        config.setActive(false);
        configRepository.save(config);

        log.info("Successfully deactivated configuration for shortcode: {} in environment: {}",
                config.getShortcode(), config.getEnvironment());
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
                textEncryptor.decrypt(config.getConsumerKey()),
                textEncryptor.decrypt(config.getConsumerSecret()),
                config.getPasskey() != null ? textEncryptor.decrypt(config.getPasskey()) : null
        );
    }

    /**
     * Validates a new configuration before creation
     */
    private void validateNewConfig(MpesaConfig config) {
        if (configRepository.existsByShortcodeAndEnvironment(
                config.getShortcode(), config.getEnvironment())) {
            throw new ConfigurationException(
                    "Configuration already exists for this shortcode and environment");
        }
    }

    /**
     * Encrypts sensitive data in the configuration
     */
    private void encryptSensitiveData(MpesaConfig config) {
        config.setConsumerKey(textEncryptor.encrypt(config.getConsumerKey()));
        config.setConsumerSecret(textEncryptor.encrypt(config.getConsumerSecret()));
        if (config.getPasskey() != null) {
            config.setPasskey(textEncryptor.encrypt(config.getPasskey()));
        }
    }

    /**
     * Updates the fields of an existing configuration
     */
    private void updateConfigFields(MpesaConfig existingConfig,
                                    MpesaConfig updatedConfig) {
        // Update encrypted fields if provided
        if (updatedConfig.getConsumerKey() != null) {
            existingConfig.setConsumerKey(textEncryptor.encrypt(updatedConfig.getConsumerKey()));
        }
        if (updatedConfig.getConsumerSecret() != null) {
            existingConfig.setConsumerSecret(textEncryptor.encrypt(updatedConfig.getConsumerSecret()));
        }
        if (updatedConfig.getPasskey() != null) {
            existingConfig.setPasskey(textEncryptor.encrypt(updatedConfig.getPasskey()));
        }

        // Update non-sensitive fields
        existingConfig.setCallbackUrl(updatedConfig.getCallbackUrl());
        existingConfig.setTimeoutUrl(updatedConfig.getTimeoutUrl());
        existingConfig.setResultUrl(updatedConfig.getResultUrl());
        existingConfig.setStkCallbackUrl(updatedConfig.getStkCallbackUrl());
        existingConfig.setActive(updatedConfig.isActive());
        existingConfig.setDescription(updatedConfig.getDescription());
    }
}
