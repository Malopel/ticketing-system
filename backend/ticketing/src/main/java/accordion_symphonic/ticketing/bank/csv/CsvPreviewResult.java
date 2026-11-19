package accordion_symphonic.ticketing.bank.csv;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CsvPreviewResult(
        int rowsRead,
        int ignored,
        int ready,
        int reviewRequired,
        List<Item> items,
        String fileSha256
) {
    public record Item(
            long rowNumber,
            LocalDate bookingDate,
            String orderReference,
            BigDecimal amount,
            Status status,
            String message
    ) {}

    public enum Status {
        READY,
        MULTIPLE_REFERENCES,
        DUPLICATE_REFERENCE,
        NON_EUR,
        NOT_CREDIT,
        ORDER_NOT_FOUND,
        ALREADY_PAID,
        WRONG_METHOD,
        NOT_PENDING,
        ORDER_EXPIRED,
        AMOUNT_MISMATCH,
        CONCERT_UNAVAILABLE
    }
}