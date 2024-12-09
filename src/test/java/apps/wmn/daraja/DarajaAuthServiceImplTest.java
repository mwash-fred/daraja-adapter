package apps.wmn.daraja;

import apps.wmn.daraja.common.config.DarajaConfig;
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
public class DarajaAuthServiceImplTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DarajaConfig darajaConfig;

    private DarajaAuthenticationServiceImpl authService;

    @BeforeEach
    void setUp() {
    authService = new DarajaAuthenticationServiceImpl(restTemplate, darajaConfig);
        when(darajaConfig.getAuthUrl()).thenReturn("https://test.url/oauth");
        when(darajaConfig.getConsumerKey()).thenReturn("testKey");
        when(darajaConfig.getConsumerSecret()).thenReturn("testSecret");
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

        String token = authService.getAccessToken();

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
        AccessTokenResponse mockResponse = new AccessTokenResponse("test-token", 3600L);

        ResponseEntity<AccessTokenResponse> responseEntity =
                new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AccessTokenResponse.class)
        )).thenReturn(responseEntity);

        String firstToken = authService.getAccessToken();
        String secondToken = authService.getAccessToken();

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
        String firstToken = authService.getAccessToken();
        String forcedToken = authService.forceNewAccessToken();

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
        assertThrows(DarajaAuthException.class, () -> authService.getAccessToken());
    }
}
