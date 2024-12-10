package apps.wmn.daraja.common.services;

import apps.wmn.daraja.common.exceptions.DarajaAuthException;

public interface DarajaAuthenticationService {
    /**
     * Retrieves a valid access token for Daraja API authentication.
     * If a valid cached token exists for the shortcode, it will be returned.
     * Otherwise, a new token will be generated.
     *
     * @param shortcode The M-Pesa shortcode to get token for
     * @param environment The environment (SANDBOX/PRODUCTION)
     * @return Valid access token string
     * @throws DarajaAuthException if token generation fails
     */
    String getAccessToken(String consumerKey, String consumerSecret, String shortcode, String environment);

    /**
     * Forces the generation of a new access token for a specific shortcode,
     * invalidating any cached token.
     *
     * @param shortcode The M-Pesa shortcode to generate token for
     * @param environment The environment (SANDBOX/PRODUCTION)
     * @return New access token
     * @throws DarajaAuthException if token generation fails
     */
    String forceNewAccessToken(String consumerKey, String consumerSecret, String shortcode, String environment);

    /**
     * Clears all cached authentication tokens.
     */
    void clearTokenCache();
}
