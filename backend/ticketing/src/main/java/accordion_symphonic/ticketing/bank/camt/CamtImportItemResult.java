package accordion_symphonic.ticketing.bank.camt;

import java.math.BigDecimal;

public record CamtImportItemResult(
        String bankReference,
        String orderReference,
        BigDecimal amount,
        Status status,
        String message
) {

    public enum Status {
        PAID,
        ALREADY_PAID,
        NO_ORDER_REFERENCE,
        ORDER_NOT_FOUND,
        NON_EUR,
        AMOUNT_MISMATCH,
        WRONG_PAYMENT_METHOD,
        ORDER_NOT_PENDING,
        ORDER_EXPIRED,
        PAYMENT_REJECTED
    }
}