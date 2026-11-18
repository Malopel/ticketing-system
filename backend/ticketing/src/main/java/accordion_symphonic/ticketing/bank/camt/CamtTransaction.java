package accordion_symphonic.ticketing.bank.camt;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CamtTransaction(
        String bankReference,
        BigDecimal amount,
        String currency,
        LocalDate bookingDate,
        String searchableText
) {
}