package accordion_symphonic.ticketing.order;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class OrderAccessTokenService {

    private static final int TOKEN_LENGTH_IN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public GeneratedOrderAccessToken generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH_IN_BYTES];
        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String tokenHash = hashToken(token);

        return new GeneratedOrderAccessToken(token, tokenHash);
    }

    public boolean matches(String providedToken, String storedTokenHash) {
        if (providedToken == null || providedToken.isBlank() || storedTokenHash == null) {
            return false;
        }

        byte[] providedHash = hashToken(providedToken).getBytes(StandardCharsets.UTF_8);

        byte[] storedHash = storedTokenHash.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(providedHash, storedHash);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public record GeneratedOrderAccessToken(String token, String tokenHash) { }
}
