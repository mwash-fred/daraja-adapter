package apps.wmn.daraja.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "daraja") @Data
public class DarajaConfig {
    private String consumerKey;
    private String consumerSecret;
    private String authUrl;
    private String baseUrl;
}
