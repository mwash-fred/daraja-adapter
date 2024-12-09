package apps.wmn.daraja.common.services.impl;

import apps.wmn.daraja.common.config.DarajaConfig;
import apps.wmn.daraja.common.dto.AccessTokenResponse;
import apps.wmn.daraja.common.exceptions.DarajaAuthException;
import apps.wmn.daraja.common.services.DarajaAuthenticationService;
import com.nimbusds.jose.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class DarajaAuthenticationServiceImpl implements DarajaAuthenticationService {

    private final RestTemplate restTemplate;
    private final DarajaConfig darajaConfig;
    private AccessTokenResponse cachedToken;
    private LocalDateTime tokenExpiryTime;

    @Override
    public String getAccessToken() {
        if (isTokenValid()) {
            log.debug("Using cached access token");
            return cachedToken.accessToken();
        }

        log.info("Generating new Daraja access token");
        return generateNewAccessToken();
    }

    @Override
    public String forceNewAccessToken() {
        log.info("Forcing generation of new access token");
        clearTokenCache();
        return generateNewAccessToken();
    }

    @Override
    public void clearTokenCache() {
        log.debug("Clearing token cache");
        this.cachedToken = null;
        this.tokenExpiryTime = null;
    }

    /**
     * Generates a new access token by calling the Daraja OAuth endpoint.
     *
     * @return Newly generated access token
     * @throws DarajaAuthException if token generation fails
     */
    private String generateNewAccessToken() {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    darajaConfig.getAuthUrl(),
                    HttpMethod.GET,
                    request,
                    AccessTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                updateTokenCache(response.getBody());
                return response.getBody().accessToken();
            }

            throw new DarajaAuthException("Failed to generate access token. Invalid response from Daraja API");

        } catch (Exception e) {
            log.error("Error generating Daraja access token", e);
            throw new DarajaAuthException("Failed to generate access token", e);
        }
    }

    /**
     * Creates HTTP headers with Basic authentication using consumer key and secret.
     *
     * @return HttpHeaders with authentication
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = darajaConfig.getConsumerKey() + ":" + darajaConfig.getConsumerSecret();
        byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.UTF_8)).decode();
        String authHeader = "Basic " + new String(encodedAuth);

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", authHeader);

        return headers;
    }

    /**
     * Updates the token cache with a new access token response.
     *
     * @param tokenResponse New access token response from Daraja API
     */
    private void updateTokenCache(AccessTokenResponse tokenResponse) {
        this.cachedToken = tokenResponse;
        // Set expiry time 3 minutes before actual expiry to ensure token validity
        this.tokenExpiryTime = LocalDateTime.now()
                .plusSeconds(tokenResponse.expiresIn())
                .minusMinutes(3);
        log.info("Access token cache updated. Valid until: {}", tokenExpiryTime);
    }

    /**
     * Checks if the cached token is still valid.
     *
     * @return true if token exists and is not expired, false otherwise
     */
    private boolean isTokenValid() {
        return cachedToken != null
                && tokenExpiryTime != null
                && LocalDateTime.now().isBefore(tokenExpiryTime);
    }

}
