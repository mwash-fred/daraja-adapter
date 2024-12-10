package apps.wmn.daraja.c2b.service;

import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing M-Pesa shortcode configurations.
 */
public interface MpesaConfigService {
    /**
     * Retrieves active configuration for a specific shortcode and environment.
     *
     * @param shortcode the M-Pesa shortcode
     * @param environment the environment (SANDBOX/PRODUCTION)
     * @return active configuration for the shortcode
     * @throws ConfigurationException if no active configuration is found
     */
    MpesaConfig getConfig(String shortcode, MpesaEnvironment environment);

    /**
     * Creates a new shortcode configuration.
     *
     * @param config the configuration to create
     * @return created configuration
     * @throws ConfigurationException if configuration already exists
     */
    MpesaConfig createConfig(MpesaConfig config);

    /**
     * Updates an existing shortcode configuration.
     *
     * @param id the configuration ID
     * @param updatedConfig the updated configuration
     * @return updated configuration
     * @throws ConfigurationException if configuration is not found
     */
    MpesaConfig updateConfig(UUID id, MpesaConfig updatedConfig);

    /**
     * Deactivates a shortcode configuration.
     *
     * @param id the configuration ID
     * @throws ConfigurationException if configuration is not found
     */
    void deactivateConfig(UUID id);

    /**
     * Lists all active configurations.
     *
     * @return list of active configurations
     */
    List<MpesaConfig> listActiveConfigs();

    /**
     * Gets decrypted credentials for a configuration.
     *
     * @param config the configuration
     * @return decrypted credentials
     */
    MpesaCredentials getDecryptedCredentials(MpesaConfig config);

    /**
     * Record to hold decrypted credentials
     */
    record MpesaCredentials(
            String consumerKey,
            String consumerSecret,
            String passkey
    ) {}
}
