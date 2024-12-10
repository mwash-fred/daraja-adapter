package apps.wmn.daraja.common.services.impl;

import apps.wmn.daraja.c2b.service.MpesaConfigService;
import apps.wmn.daraja.common.dto.AccessTokenResponse;
import apps.wmn.daraja.common.exceptions.DarajaAuthException;
import apps.wmn.daraja.common.services.DarajaAuthenticationService;
import com.nimbusds.jose.util.Base64;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class DarajaAuthenticationServiceImpl implements DarajaAuthenticationService {

    private final RestTemplate restTemplate;
    private final MpesaConfigService configService;

    // Cache structure: shortcode_environment -> token details
    private final Map<String, TokenDetails> tokenCache = new ConcurrentHashMap<>();

    @Override
    @Cacheable(value = "authTokens", key = "#shortcode + '_' + #environment")
    public String getAccessToken(String consumerKey, String consumerSecret, String shortcode, String environment){
        String cacheKey = shortcode + "_" + environment;
        TokenDetails cachedDetails = tokenCache.get(cacheKey);

        if (isTokenValid(cachedDetails)) {
            log.debug("Using cached access token for shortcode: {}", shortcode);
            return cachedDetails.token;
        }

        log.info("Generating new Daraja access token for shortcode: {}", shortcode);
        return generateNewAccessToken(shortcode, environment, consumerKey, consumerSecret);
    }

    @Override
    @CacheEvict(value = "authTokens", key = "#shortcode + '_' + #environment")
    public String forceNewAccessToken(String consumerKey, String consumerSecret, String shortcode, String environment) {
        log.info("Forcing generation of new access token for shortcode: {}", shortcode);
        String cacheKey = shortcode + "_" + environment;
        tokenCache.remove(cacheKey);
        return generateNewAccessToken(shortcode, environment, consumerKey, consumerSecret);
    }

    @Override
    @CacheEvict(value = "authTokens", allEntries = true)
    public void clearTokenCache() {
        log.debug("Clearing all token caches");
        tokenCache.clear();
    }

    private String generateNewAccessToken(String shortcode, String environment, String consumerKey, String consumerSecret) {
        try {
            // Prepare request
            HttpHeaders headers = createAuthHeaders(consumerKey, consumerSecret);
            HttpEntity<String> request = new HttpEntity<>(headers);

            String authUrl = getAuthUrl(environment);

            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    authUrl,
                    HttpMethod.GET,
                    request,
                    AccessTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                updateTokenCache(shortcode, environment, response.getBody());
                return response.getBody().accessToken();
            }

            throw new DarajaAuthException("Failed to generate access token. Invalid response from Daraja API");

        } catch (Exception e) {
            log.error("Error generating Daraja access token for shortcode: {}", shortcode, e);
            throw new DarajaAuthException("Failed to generate access token", e);
        }
    }

    private HttpHeaders createAuthHeaders(String consumerKey, String consumerSecret) {
        HttpHeaders headers = new HttpHeaders();
        String auth = consumerKey + ":" + consumerSecret;
        byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.UTF_8)).decode();
        String authHeader = "Basic " + new String(encodedAuth);

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", authHeader);

        return headers;
    }

    private void updateTokenCache(String shortcode, String environment, AccessTokenResponse tokenResponse) {
        String cacheKey = shortcode + "_" + environment;
        TokenDetails details = new TokenDetails(
                tokenResponse.accessToken(),
                LocalDateTime.now()
                        .plusSeconds(tokenResponse.expiresIn())
                        .minusMinutes(3)
        );
        tokenCache.put(cacheKey, details);
        log.info("Access token cache updated for shortcode: {}. Valid until: {}",
                shortcode, details.expiryTime);
    }

    private boolean isTokenValid(TokenDetails details) {
        return details != null && LocalDateTime.now().isBefore(details.expiryTime);
    }

    private String getAuthUrl(String environment) {
        String baseUrl = "PRODUCTION".equalsIgnoreCase(environment)
                ? "https://api.safaricom.co.ke"
                : "https://sandbox.safaricom.co.ke";
        return baseUrl + "/oauth/v1/generate?grant_type=client_credentials";
    }

    /**
     * Internal record to hold token details with expiry time
     */
    private record TokenDetails(
            String token,
            LocalDateTime expiryTime
    ) {}

}
