package apps.wmn.daraja.c2b.entity;

import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import apps.wmn.daraja.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mpesa_shortcode_configs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MpesaConfig extends BaseEntity {
    @Column(name = "shortcode")
    private String shortcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment")
    private MpesaEnvironment environment;

    @Column(name = "consumer_key")
    private String consumerKey;

    @Column(name = "consumer_secret")
    private String consumerSecret;

    @Column(name = "passkey")
    private String passkey;

    @Column(name = "callback_url")
    private String callbackUrl;

    @Column(name = "timeout_url")
    private String timeoutUrl;

    @Column(name = "result_url")
    private String resultUrl;

    @Column(name = "stk_callback_url")
    private String stkCallbackUrl;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "description")
    private String description;

}
