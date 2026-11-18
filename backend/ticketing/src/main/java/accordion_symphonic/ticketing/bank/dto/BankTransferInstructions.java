package accordion_symphonic.ticketing.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankTransferInstructions(
        String recipient,
        String iban,
        String bic,
        BigDecimal amount,
        String reference,
        LocalDateTime expiresAt,
        String paytoUri,
        String epcQrPayload
) {
}