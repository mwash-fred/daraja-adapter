package apps.wmn.daraja.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mpesa")
@Data

public class MpesaUrlConfig {
    private Urls urls;

    @Data
    public static class Urls {
        private Environment sandbox;
        private Environment prod;
    }

    @Data
    public static class Environment {
        private String registerUrl;
        private String authUrl;
        private String stkPushUrl;
    }
}
