package accordion_symphonic.ticketing.order.exception;

public class OrderCannotBeCancelledException extends RuntimeException {

    public OrderCannotBeCancelledException(Long id) {
        super("Order with id " + id + " is paid or expired");
    }
}
