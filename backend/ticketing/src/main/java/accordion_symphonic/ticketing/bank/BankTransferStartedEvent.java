package accordion_symphonic.ticketing.bank;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankTransferStartedEvent(
        Long orderId,
        String customerEmail,
        String concertTitle,
        String publicId,
        BigDecimal amount,
        LocalDateTime paymentExpiresAt
) {}