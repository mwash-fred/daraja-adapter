package apps.wmn.daraja.c2b.repository;

import apps.wmn.daraja.c2b.entity.MpesaPayment;
import apps.wmn.daraja.c2b.enums.TransactionStatus;
import apps.wmn.daraja.c2b.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MpesaPaymentRepository extends JpaRepository<MpesaPayment, Long> , JpaSpecificationExecutor<MpesaPayment> {
    /**
     * Find payment by M-Pesa transaction ID
     */
    Optional<MpesaPayment> findByTransactionId(String transactionId);

    Optional<MpesaPayment> findByUuid(UUID uuid);

    /**
     * Find all payments for a specific phone number, ordered by creation date
     */
    List<MpesaPayment> findByPhoneNumberOrderByCreatedDateDesc(String phoneNumber);

    /**
     * Find payments by status and type
     */
    List<MpesaPayment> findByTransactionStatusAndTransactionType(
            TransactionStatus status,
            TransactionType type
    );

    /**
     * Find payments with dynamic filters
     */
    @Query("""
        SELECT p FROM MpesaPayment p
        WHERE (:phoneNumber IS NULL OR p.phoneNumber = :phoneNumber)
        AND (:status IS NULL OR p.transactionStatus = :status)
        AND (:startDate IS NULL OR p.createdDate >= :startDate)
        AND (:endDate IS NULL OR p.createdDate <= :endDate)
        ORDER BY p.createdDate DESC
        """)
    Page<MpesaPayment> findPayments(
            @Param("phoneNumber") String phoneNumber,
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Count payments by status
     */
    long countByTransactionStatus(TransactionStatus status);

    /**
     * Find pending payments that need retry
     */
    @Query("""
        SELECT p FROM MpesaPayment p
        WHERE p.transactionStatus = 'PENDING'
        AND p.retryCount < :maxRetries
        AND p.createdDate <= :cutoffTime
        ORDER BY p.createdDate ASC
        """)
    List<MpesaPayment> findPendingPaymentsForRetry(
            @Param("maxRetries") int maxRetries,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    /**
     * Find payments by account reference
     */
    List<MpesaPayment> findByAccountReferenceOrderByCreatedDateDesc(String accountReference);

    /**
     * Find payments within a date range
     */
    @Query("""
        SELECT p FROM MpesaPayment p
        WHERE p.createdDate BETWEEN :startDate AND :endDate
        ORDER BY p.createdDate DESC
        """)
    List<MpesaPayment> findPaymentsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find payments that have been stuck in pending state
     */
    @Query("""
        SELECT p FROM MpesaPayment p
        WHERE p.transactionStatus = 'PENDING'
        AND p.createdDate <= :stuckThreshold
        ORDER BY p.createdDate ASC
        """)
    List<MpesaPayment> findStuckPayments(
            @Param("stuckThreshold") LocalDateTime stuckThreshold
    );

    /**
     * Find payments by bill reference number
     */
    Optional<MpesaPayment> findByBillRefNumber(String billRefNumber);

    /**
     * Find payments by phone number and status
     */
    List<MpesaPayment> findByPhoneNumberAndTransactionStatus(
            String phoneNumber,
            TransactionStatus status
    );

    /**
     * Count payments by type
     */
    @Query("""
        SELECT p.transactionType, COUNT(p)
        FROM MpesaPayment p
        WHERE p.createdDate BETWEEN :startDate AND :endDate
        GROUP BY p.transactionType
        """)
    List<Object[]> countPaymentsByType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Delete old completed payments (for cleanup)
     */
    @Query("""
        DELETE FROM MpesaPayment p
        WHERE p.transactionStatus = 'COMPLETED'
        AND p.createdDate <= :retentionDate
        """)
    void deleteOldCompletedPayments(@Param("retentionDate") LocalDateTime retentionDate);
}
