package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.payment.service.PaymentWebhookSignatureService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentWebhookSignatureServiceTest {

    private static final String REQUEST_BODY =
            "The quick brown fox jumps over the lazy dog";

    private static final String VALID_SIGNATURE =
            "f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8";

    private final PaymentWebhookSignatureService signatureService =
            new PaymentWebhookSignatureService("key");

    @Test
    void validSignatureIsAccepted() {
        assertTrue(
                signatureService.isValid(
                        REQUEST_BODY,
                        VALID_SIGNATURE
                )
        );
    }

    @Test
    void changedRequestBodyIsRejected() {
        assertFalse(
                signatureService.isValid(
                        REQUEST_BODY + "!",
                        VALID_SIGNATURE
                )
        );
    }

    @Test
    void wrongSignatureIsRejected() {
        assertFalse(
                signatureService.isValid(
                        REQUEST_BODY,
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                )
        );
    }

    @Test
    void malformedSignatureIsRejected() {
        assertFalse(
                signatureService.isValid(
                        REQUEST_BODY,
                        "not-a-hex-signature"
                )
        );
    }

    @Test
    void missingSignatureIsRejected() {
        assertFalse(signatureService.isValid(REQUEST_BODY, null));
        assertFalse(signatureService.isValid(REQUEST_BODY, ""));
        assertFalse(signatureService.isValid(REQUEST_BODY, "   "));
    }
}