package apps.wmn.daraja.c2b.internal;

import apps.wmn.daraja.c2b.dto.MpesaUrlRegistrationRequest;
import apps.wmn.daraja.c2b.dto.MpesaUrlRegistrationResponse;
import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import apps.wmn.daraja.c2b.repository.MpesaConfigRepository;
import apps.wmn.daraja.common.config.MpesaUrlConfig;
import apps.wmn.daraja.common.event.CallbackValidationUrlRegistrationEvent;
import apps.wmn.daraja.common.exceptions.ConfigurationException;
import apps.wmn.daraja.common.services.DarajaAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class C2bUrlRegistrationEventListener {
  private final MpesaConfigRepository configRepository;
  private final DarajaAuthenticationService darajaAuthenticationService;
  private final MpesaUrlConfig mpesaUrlConfig;
  private final RestTemplate restTemplate;

  @ApplicationModuleListener
  public void onCallbackValidationUrlRegistration(CallbackValidationUrlRegistrationEvent event) {
    log.debug("Received callback validation URL registration event: {}", event);

    MpesaConfig config = getMpesaConfig(event);
    log.info("Mpesa config: {}", config);
    String accessToken = authenticateWithDaraja(config);
    log.info("Access token: {}", accessToken);
    MpesaUrlRegistrationResponse response = registerCallbackUrls(event, accessToken);

    log.info("URL registration response: {}", response);
  }

  private MpesaConfig getMpesaConfig(CallbackValidationUrlRegistrationEvent event) {
    return configRepository
            .findByShortcodeAndEnvironment(event.shortcode(), event.environment())
            .orElseThrow(() -> new ConfigurationException("Configuration not found"));
  }

  private String authenticateWithDaraja(MpesaConfig config) {
    return darajaAuthenticationService.getAccessToken(
            config.getConsumerKey(),
            config.getConsumerSecret(),
            config.getShortcode(),
            config.getEnvironment()
    );
  }

  private MpesaUrlRegistrationResponse registerCallbackUrls(
          CallbackValidationUrlRegistrationEvent event,
          String accessToken
  ) {
    HttpEntity<MpesaUrlRegistrationRequest> httpEntity = createHttpEntity(event, accessToken);
    String registrationUrl = getRegistrationUrl(event.environment());

    log.info("HttpEntity: {}", httpEntity);

    return restTemplate.postForObject(
            registrationUrl,
            httpEntity,
            MpesaUrlRegistrationResponse.class
    );
  }

  private HttpEntity<MpesaUrlRegistrationRequest> createHttpEntity(
          CallbackValidationUrlRegistrationEvent event,
          String accessToken
  ) {
    return new HttpEntity<>(
            createRegistrationRequest(event),
            createHeaders(accessToken)
    );
  }

  private MpesaUrlRegistrationRequest createRegistrationRequest(CallbackValidationUrlRegistrationEvent event) {
    return new MpesaUrlRegistrationRequest(
            event.shortcode(),
            "Completed",
            event.confirmationUrl(),
            event.validationUrl()
    );
  }

  private HttpHeaders createHeaders(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(accessToken);
    return headers;
  }

  private String getRegistrationUrl(MpesaEnvironment environment) {
    return environment.equals(MpesaEnvironment.SANDBOX)
            ? mpesaUrlConfig.getUrls().getSandbox().getRegisterUrl()
            : mpesaUrlConfig.getUrls().getProd().getRegisterUrl();
  }
}