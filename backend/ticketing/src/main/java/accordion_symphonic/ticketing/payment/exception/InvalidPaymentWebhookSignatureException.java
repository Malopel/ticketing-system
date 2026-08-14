package accordion_symphonic.ticketing.payment.exception;

public class InvalidPaymentWebhookSignatureException extends RuntimeException {

  public InvalidPaymentWebhookSignatureException() {
    super("Invalid payment webhook signature");
  }
}