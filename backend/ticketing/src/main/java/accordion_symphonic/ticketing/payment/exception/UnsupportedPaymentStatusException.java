package accordion_symphonic.ticketing.payment.exception;

import accordion_symphonic.ticketing.payment.PaymentStatus;

public class UnsupportedPaymentStatusException extends RuntimeException {

    public UnsupportedPaymentStatusException(PaymentStatus status) {
        super("Unsupported payment status: " + status);
    }
}