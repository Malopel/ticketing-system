package accordion_symphonic.ticketing.payment;

public class InvalidPaymentWebhookSignatureException extends RuntimeException {

  public InvalidPaymentWebhookSignatureException() {
    super("Invalid payment webhook signature");
  }
}