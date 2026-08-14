package accordion_symphonic.ticketing.payment.exception;

public class OrderCannotBePaidException extends RuntimeException {

    public OrderCannotBePaidException(Long orderId) {
        super("Order cannot be paid: " + orderId);
    }
}