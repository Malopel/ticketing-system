package accordion_symphonic.ticketing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password",
        "ticketing.payment.webhook-secret=test-webhook-secret",
        "ticketing.payment.provider=fake"
})
class TicketingApplicationTests {

    @Test
    void contextLoads() {
    }
}