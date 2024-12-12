package apps.wmn.daraja.c2b.service;

import apps.wmn.daraja.c2b.dto.*;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for handling M-Pesa payment operations
 */
public interface MpesaPaymentService {

    /**
     * Initiates an STK push request
     */
    StkPushResponse initiateSTKPush(StkPushRequest request, MpesaEnvironment environment);

    /**
     * Processes STK callback from M-Pesa
     */
    PaymentView processStkCallback(StkCallback callback);

    /**
     * Processes C2B payment callback
     */
    PaymentView processC2BCallback(C2bCallback callback);

    /**
     * Retrieves payment by ID
     */
    PaymentView getPayment(UUID paymentId);

    /**
     * Retrieves payment by transaction ID
     */
    PaymentView getPaymentByTransactionId(String transactionId);

    /**
     * Retrieves filtered paginated payments
     */
    Page<PaymentView> getPayments(
            String phoneNumber,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Retrieves payments for a phone number
     */
    List<PaymentView> getPaymentsByPhoneNumber(String phoneNumber);

    /**
     * Validates payment status with M-Pesa
     */
    PaymentView validatePaymentStatus(String transactionId);
}