package accordion_symphonic.ticketing.order.exception;

public class OrderCannotBePaidException extends Throwable {
    public OrderCannotBePaidException(Long orderId) {
        super("Order with ID " + orderId + " cannot be paid");
    }
}
