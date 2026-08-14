package accordion_symphonic.ticketing.order.exception;

public class TooManyTicketsInOrderException extends RuntimeException {

    public TooManyTicketsInOrderException(int requestedTickets, int maxTicketsPerOrder) {
        super("Too many tickets in order: requested " + requestedTickets
                + ", maximum allowed is " + maxTicketsPerOrder);
    }
}