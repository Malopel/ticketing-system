package accordion_symphonic.ticketing.payment;

public record PaymentSession(
        String providerPaymentId,
        String checkoutUrl
) {
}