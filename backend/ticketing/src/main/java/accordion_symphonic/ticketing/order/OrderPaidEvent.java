package accordion_symphonic.ticketing.order;

public record OrderPaidEvent(
        Long concertId,
        Long orderId
) {
}