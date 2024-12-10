package apps.wmn.daraja;

import apps.wmn.daraja.c2b.service.MpesaConfigService;
import apps.wmn.daraja.common.dto.AccessTokenResponse;
import apps.wmn.daraja.common.exceptions.DarajaAuthException;
import apps.wmn.daraja.common.services.impl.DarajaAuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DarajaAuthServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MpesaConfigService configService;

    private DarajaAuthenticationServiceImpl authService;

    private static final String TEST_SHORTCODE = "174379";
    private static final String TEST_CONSUMER_KEY = "testKey";
    private static final String TEST_CONSUMER_SECRET = "testSecret";
    private static final String TEST_ENVIRONMENT = "sandbox";

    @BeforeEach
    void setUp() {
        authService = new DarajaAuthenticationServiceImpl(restTemplate, configService);
    }

    @Test
    void getAccessToken_Success() {
        // Arrange
        AccessTokenResponse mockResponse = new AccessTokenResponse("test-token", 3600L);
        ResponseEntity<AccessTokenResponse> responseEntity =
                new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        )).thenReturn(responseEntity);

        // Act
        String token = authService.getAccessToken(
                TEST_CONSUMER_KEY,
                TEST_CONSUMER_SECRET,
                TEST_SHORTCODE,
                TEST_ENVIRONMENT
        );

        // Assert
        assertEquals("test-token", token);
        verify(restTemplate, times(1)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        );
    }

    @Test
    void getAccessToken_UsesCachedToken() {
        // Arrange
        AccessTokenResponse mockResponse = new AccessTokenResponse("test-token", 3600L);
        ResponseEntity<AccessTokenResponse> responseEntity =
                new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        )).thenReturn(responseEntity);

        // Act
        String firstToken = authService.getAccessToken(
                TEST_CONSUMER_KEY,
                TEST_CONSUMER_SECRET,
                TEST_SHORTCODE,
                TEST_ENVIRONMENT
        );
        String secondToken = authService.getAccessToken(
                TEST_CONSUMER_KEY,
                TEST_CONSUMER_SECRET,
                TEST_SHORTCODE,
                TEST_ENVIRONMENT
        );

        // Assert
        assertEquals("test-token", firstToken);
        assertEquals("test-token", secondToken);
        verify(restTemplate, times(1)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        );
    }

    @Test
    void forceNewAccessToken_GeneratesNewToken() {
        // Arrange
        AccessTokenResponse mockResponse = new AccessTokenResponse("test-token", 3600L);
        ResponseEntity<AccessTokenResponse> responseEntity =
                new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        )).thenReturn(responseEntity);

        // Act
        String firstToken = authService.getAccessToken(
                TEST_CONSUMER_KEY,
                TEST_CONSUMER_SECRET,
                TEST_SHORTCODE,
                TEST_ENVIRONMENT
        );
        String forcedToken = authService.forceNewAccessToken(
                TEST_CONSUMER_KEY,
                TEST_CONSUMER_SECRET,
                TEST_SHORTCODE,
                TEST_ENVIRONMENT
        );

        // Assert
        assertEquals("test-token", firstToken);
        assertEquals("test-token", forcedToken);
        verify(restTemplate, times(2)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        );
    }

    @Test
    void getAccessToken_ThrowsException_WhenApiFails() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        )).thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        assertThrows(DarajaAuthException.class, () ->
                authService.getAccessToken(
                        TEST_CONSUMER_KEY,
                        TEST_CONSUMER_SECRET,
                        TEST_SHORTCODE,
                        TEST_ENVIRONMENT
                )
        );
    }

    @Test
    void getAccessToken_DifferentShortcodes_MaintainsSeparateTokens() {
        // Arrange
        String secondShortcode = "654321";
        AccessTokenResponse mockResponse1 = new AccessTokenResponse("token-1", 3600L);
        AccessTokenResponse mockResponse2 = new AccessTokenResponse("token-2", 3600L);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        )).thenReturn(
                new ResponseEntity<>(mockResponse1, HttpStatus.OK),
                new ResponseEntity<>(mockResponse2, HttpStatus.OK)
        );

        // Act
        String token1 = authService.getAccessToken(
                TEST_CONSUMER_KEY,
                TEST_CONSUMER_SECRET,
                TEST_SHORTCODE,
                TEST_ENVIRONMENT
        );
        String token2 = authService.getAccessToken(
                TEST_CONSUMER_KEY,
                TEST_CONSUMER_SECRET,
                secondShortcode,
                TEST_ENVIRONMENT
        );

        // Assert
        assertNotEquals(token1, token2);
        verify(restTemplate, times(2)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        );
    }

    @Test
    void clearTokenCache_InvalidatesAllTokens() {
        // Arrange
        AccessTokenResponse mockResponse = new AccessTokenResponse("test-token", 3600L);
        ResponseEntity<AccessTokenResponse> responseEntity =
                new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        )).thenReturn(responseEntity);

        // Act
        String firstToken = authService.getAccessToken(
                TEST_CONSUMER_KEY,
                TEST_CONSUMER_SECRET,
                TEST_SHORTCODE,
                TEST_ENVIRONMENT
        );
        authService.clearTokenCache();
        String secondToken = authService.getAccessToken(
                TEST_CONSUMER_KEY,
                TEST_CONSUMER_SECRET,
                TEST_SHORTCODE,
                TEST_ENVIRONMENT
        );

        // Assert
        verify(restTemplate, times(2)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        );
    }
}