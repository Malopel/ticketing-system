package accordion_symphonic.ticketing.payment;

public record PaymentWebhookRequest(
        String eventId,
        Long orderId,
        PaymentStatus status
) {
}
