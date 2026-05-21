package accordion_symphonic.ticketing.order;

public class OrderIsExpiredOrCancelledException extends RuntimeException {

    public OrderIsExpiredOrCancelledException(Long id) {
        super("Order with id " + id + " is expired or cancelled");
    }
}
