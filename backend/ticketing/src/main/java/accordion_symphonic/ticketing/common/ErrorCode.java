package accordion_symphonic.ticketing.common;

public final class ErrorCode {

    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    public static final String CONCERT_NOT_FOUND = "CONCERT_NOT_FOUND";
    public static final String TICKET_CATEGORY_NOT_FOUND = "TICKET_CATEGORY_NOT_FOUND";

    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
    public static final String ORDER_CANNOT_BE_PAID = "ORDER_CANNOT_BE_PAID";
    public static final String ORDER_HAS_NO_TICKETS = "ORDER_HAS_NO_TICKETS";

    public static final String NOT_ENOUGH_TICKETS_AVAILABLE = "NOT_ENOUGH_TICKETS_AVAILABLE";
    public static final String TOO_MANY_TICKETS_IN_ORDER = "TOO_MANY_TICKETS_IN_ORDER";
    public static final String DUPLICATE_TICKET_CATEGORY = "DUPLICATE_TICKET_CATEGORY";

    public static final String INVALID_PAYMENT_SIGNATURE = "INVALID_PAYMENT_SIGNATURE";

    public static final String INVALID_TICKET = "INVALID_TICKET";
    public static final String TICKET_NOT_FOUND = "TICKET_NOT_FOUND";
    private ErrorCode() {
    }
}