package apps.wmn.daraja.c2b.entity;

import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import apps.wmn.daraja.common.entity.BaseEntity;
import apps.wmn.daraja.common.enums.ShortcodeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "mpesa_shortcode_configs",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"shortcode", "environment"},
                        name = "unique_shortcode_env")
        })
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

  @Enumerated(EnumType.STRING)
  @Column(name = "shortcode_type")
  private ShortcodeType shortcodeType;

  @Column(name = "consumer_key")
  private String consumerKey;

  @Column(name = "consumer_secret")
  private String consumerSecret;

  @Column(name = "passkey")
  private String passkey;

  @Column(name = "initiator_name")
  private String initiatorName;

  @Column(name = "security_credential")
  private String securityCredential;

  @Column(name = "collection_callback_url")
  private String collectionCallbackUrl;

  @Column(name = "collection_validation_url")
  private String collectionValidationUrl;

  @Column(name = "collection_timeout_url")
  private String collectionTimeoutUrl;

  @Column(name = "collection_result_url")
  private String collectionResultUrl;

  @Column(name = "disbursement_result_url")
  private String disbursementResultUrl;

  @Column(name = "disbursement_timeout_url")
  private String disbursementTimeoutUrl;

  @Column(name = "disbursement_queue_url")
  private String disbursementQueueUrl;

  @Column(name = "stk_callback_url")
  private String stkCallbackUrl;

  @Column(name = "is_active")
  private boolean active = true;

  @Column(name = "description")
  private String description;
}