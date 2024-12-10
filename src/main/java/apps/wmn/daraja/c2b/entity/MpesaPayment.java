package apps.wmn.daraja.c2b.entity;

import apps.wmn.daraja.c2b.enums.TransactionStatus;
import apps.wmn.daraja.c2b.enums.TransactionType;
import apps.wmn.daraja.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "mpesa_payments") @Getter
@Setter @AllArgsConstructor @NoArgsConstructor @SuperBuilder
public class MpesaPayment extends BaseEntity {
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "account_reference")
    private String accountReference;

    @Column(name = "transaction_desc")
    private String transactionDesc;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status")
    private TransactionStatus transactionStatus;

    @Column(name = "business_short_code")
    private String businessShortCode;

    @Column(name = "bill_ref_number")
    private String billRefNumber;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "org_account_balance")
    private BigDecimal orgAccountBalance;

    @Column(name = "third_party_trans_id")
    private String thirdPartyTransId;

    // Customer details
    @Column(name = "msisdn")
    private String msisdn;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    // Audit fields
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

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

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;
}
