package apps.wmn.daraja.common.services;

import apps.wmn.daraja.common.exceptions.DarajaAuthException;

public interface DarajaAuthenticationService {
    /**
     * Retrieves a valid access token for Daraja API authentication.
     * If a valid cached token exists, it will be returned.
     * Otherwise, a new token will be generated.
     *
     * @return Valid access token string
     * @throws DarajaAuthException if token generation fails
     */
    String getAccessToken();

    /**
     * Forces the generation of a new access token, invalidating any cached token.
     * This can be used when a token is rejected by the API despite not being expired.
     *
     * @return New access token
     * @throws DarajaAuthException if token generation fails
     */
    String forceNewAccessToken();

    /**
     * Clears any cached authentication tokens.
     * Useful for testing and handling token invalidation scenarios.
     */
    void clearTokenCache();
}
