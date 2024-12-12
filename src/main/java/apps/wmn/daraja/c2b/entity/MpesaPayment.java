package apps.wmn.daraja.c2b.entity;

import apps.wmn.daraja.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mpesa_payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MpesaPayment extends BaseEntity {

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "origin_transaction_id")
    private String originTransactionId;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transaction_status")
    private String transactionStatus;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "charges_amount")
    private BigDecimal chargesAmount;

    @Column(name = "org_account_balance")
    private BigDecimal orgAccountBalance;

    @Column(name = "business_short_code")
    private String businessShortCode;

    @Column(name = "initiator_identifier")
    private String initiatorIdentifier;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "payer_identifier")
    private String payerIdentifier;

    @Column(name = "payee_identifier")
    private String payeeIdentifier;

    @Column(name = "account_reference")
    private String accountReference;

    @Column(name = "bill_ref_number")
    private String billRefNumber;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "transaction_desc")
    private String transactionDesc;

    @Column(name = "third_party_trans_id")
    private String thirdPartyTransId;

    @Column(name = "msisdn")
    private String msisdn;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "raw_request")
    @JdbcTypeCode(SqlTypes.JSON)
    private String rawRequest;

    @Column(name = "raw_callback")
    @JdbcTypeCode(SqlTypes.JSON)
    private String rawCallback;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;
}