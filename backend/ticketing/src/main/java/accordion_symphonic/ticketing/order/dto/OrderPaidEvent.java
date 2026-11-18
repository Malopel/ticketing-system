package accordion_symphonic.ticketing.order.dto;

public record OrderPaidEvent(
        Long concertId,
        Long orderId
) {
}