package accordion_symphonic.ticketing.payment;

public class UnsupportedPaymentStatusException extends RuntimeException {

    public UnsupportedPaymentStatusException(PaymentStatus status) {
        super("Unsupported payment status: " + status);
    }
}