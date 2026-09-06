package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.mail.TicketEmailService;
import accordion_symphonic.ticketing.order.*;
import accordion_symphonic.ticketing.payment.dto.PaymentWebhookRequest;
import accordion_symphonic.ticketing.payment.service.PaymentWebhookService;
import accordion_symphonic.ticketing.ticket.TicketRepository;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password",
        "ticketing.payment.webhook-secret=test-webhook-secret",
        "ticketing.payment.provider=fake"
})
class PaymentWebhookServiceIntegrationTest {

    private static final String WEBHOOK_SECRET = "test-webhook-secret";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private PaymentWebhookService paymentWebhookService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @MockitoBean
    private TicketEmailService ticketEmailService;

    @Test
    void paidWebhookMarksOrderPaidCreatesTicketsAndIsIdempotent() {
        Concert concert = new Concert(
                "Testkonzert",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Konzertsaal"
        );

        concert.publish();

        concert = concertRepository.save(concert);

        TicketCategory category = ticketCategoryRepository.save(
                new TicketCategory(
                        "Normalpreis",
                        new BigDecimal("25.00"),
                        100,
                        concert
                )
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        order.addItem(
                new OrderItem(
                        category,
                        2,
                        new BigDecimal("25.00")
                )
        );

        order.markAsPaymentPending(
                LocalDateTime.now().plusMinutes(20)
        );

        Order savedOrder = orderRepository.save(order);

        String rawBody = """
                {"eventId":"evt_integration_001","orderId":%d,"status":"PAID"}
                """.formatted(savedOrder.getId()).trim();

        String signature = sign(rawBody);

        PaymentWebhookRequest request = new PaymentWebhookRequest(
                "evt_integration_001",
                savedOrder.getId(),
                PaymentStatus.PAID
        );

        paymentWebhookService.processWebhook(
                rawBody,
                signature,
                request
        );

        Order paidOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow();

        assertEquals(OrderStatus.PAID, paidOrder.getStatus());
        assertEquals(1, ticketRepository.findByOrderId(savedOrder.getId()).size() / 2);
        assertEquals(2, ticketRepository.findByOrderId(savedOrder.getId()).size());

        paymentWebhookService.processWebhook(
                rawBody,
                signature,
                request
        );

        assertEquals(2, ticketRepository.findByOrderId(savedOrder.getId()).size());
        verify(ticketEmailService, times(1)).sendEmail(any(Order.class), anyList());
    }

    private String sign(String rawBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(
                    new SecretKeySpec(
                            WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    )
            );

            byte[] signature = mac.doFinal(
                    rawBody.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(signature);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}