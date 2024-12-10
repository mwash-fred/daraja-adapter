package apps.wmn.daraja.c2b.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * View DTOs
 */
@Schema(description = "Payment details view")
public record PaymentView(
        UUID uuid,
        String transactionId,
        String phoneNumber,
        BigDecimal amount,
        String currency,
        String accountReference,
        String transactionDesc,
        String transactionType,
        String transactionStatus,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
}
