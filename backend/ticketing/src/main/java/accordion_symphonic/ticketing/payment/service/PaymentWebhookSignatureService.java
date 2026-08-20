package accordion_symphonic.ticketing.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class PaymentWebhookSignatureService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKey;

    public PaymentWebhookSignatureService(
            @Value("${ticketing.payment.webhook-secret}") String webhookSecret
    ) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment webhook secret must not be blank"
            );
        }

        this.secretKey = new SecretKeySpec(
                webhookSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_ALGORITHM
        );
    }

    public boolean isValid(String rawRequestBody, String providedSignature) {
        if (rawRequestBody == null
                || providedSignature == null
                || providedSignature.isBlank()) {
            return false;
        }

        try {
            byte[] expectedSignature = calculateSignature(rawRequestBody);
            byte[] receivedSignature = HexFormat.of()
                    .parseHex(providedSignature.trim());

            return MessageDigest.isEqual(
                    expectedSignature,
                    receivedSignature
            );
        } catch (IllegalArgumentException exception) {
            // Die gelieferte Signatur war kein gültiger Hex-String.
            return false;
        }
    }

    private byte[] calculateSignature(String rawRequestBody) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKey);

            return mac.doFinal(
                    rawRequestBody.getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new IllegalStateException(
                    "Could not calculate payment webhook signature",
                    exception
            );
        }
    }
}