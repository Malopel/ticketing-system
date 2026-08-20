package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.order.service.OrderAccessTokenService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderAccessTokenServiceTest {

    private final OrderAccessTokenService tokenService =
            new OrderAccessTokenService();

    @Test
    void generatedTokenMatchesItsHash() {
        OrderAccessTokenService.GeneratedOrderAccessToken generated =
                tokenService.generateToken();

        assertTrue(
                tokenService.matches(
                        generated.token(),
                        generated.tokenHash()
                )
        );
    }

    @Test
    void differentTokenDoesNotMatchHash() {
        OrderAccessTokenService.GeneratedOrderAccessToken generated =
                tokenService.generateToken();

        assertFalse(
                tokenService.matches(
                        "wrong-token",
                        generated.tokenHash()
                )
        );
    }

    @Test
    void generatedTokensAreDifferent() {
        OrderAccessTokenService.GeneratedOrderAccessToken first =
                tokenService.generateToken();

        OrderAccessTokenService.GeneratedOrderAccessToken second =
                tokenService.generateToken();

        assertNotEquals(first.token(), second.token());
        assertNotEquals(first.tokenHash(), second.tokenHash());
    }

    @Test
    void missingTokenDoesNotMatchStoredHash() {
        OrderAccessTokenService.GeneratedOrderAccessToken generatedToken =
                tokenService.generateToken();

        assertFalse(tokenService.matches(null, generatedToken.tokenHash()));
        assertFalse(tokenService.matches("", generatedToken.tokenHash()));
        assertFalse(tokenService.matches("   ", generatedToken.tokenHash()));
    }
}