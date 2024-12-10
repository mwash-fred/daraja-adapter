package apps.wmn.daraja.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class EncryptionConfig {
    @Value("${encryption.secret-key}")
    private String secretKey;

    @Value("${encryption.salt}")
    private String salt;

    @Bean
    public TextEncryptor textEncryptor() {
        // Using Spring's Encryptors utility to create a text encryptor
        // This uses 256-bit AES encryption with PKCS #5 padding
        return Encryptors.text(secretKey, salt);
    }
}
