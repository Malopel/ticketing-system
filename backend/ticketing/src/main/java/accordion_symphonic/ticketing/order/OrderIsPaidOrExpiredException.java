package accordion_symphonic.ticketing.order;

public class OrderIsPaidOrExpiredException extends RuntimeException {

    public OrderIsPaidOrExpiredException(Long id) {
        super("Order with id " + id + " is paid or expired");
    }
}
