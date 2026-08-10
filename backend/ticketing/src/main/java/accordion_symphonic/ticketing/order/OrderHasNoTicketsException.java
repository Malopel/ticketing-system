package accordion_symphonic.ticketing.order;

public class OrderHasNoTicketsException extends RuntimeException {

    public OrderHasNoTicketsException(Long orderId) {
        super("Order has no generated tickets: " + orderId);
    }
}