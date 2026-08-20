package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.ticket.exception.TicketIsNotValidException;
import accordion_symphonic.ticketing.ticket.service.TicketValidationService;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password",
        "ticketing.payment.webhook-secret=test-webhook-secret",
        "ticketing.payment.provider=fake"
})
class TicketConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private TicketValidationService ticketValidationService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void sameTicketCannotBeUsedTwiceConcurrently()
            throws Exception {

        Concert concert = new Concert(
                "Concurrency Konzert",
                "Test",
                LocalDateTime.now().plusDays(30),
                "Karlsruhe"
        );

        Concert savedConcert =
                concertRepository.save(concert);

        TicketCategory category =
                ticketCategoryRepository.save(
                        new TicketCategory(
                                "Normalpreis",
                                new BigDecimal("25.00"),
                                100,
                                savedConcert
                        )
                );

        Order order = orderRepository.save(
                new Order(
                        savedConcert,
                        "kunde@example.com",
                        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(30)
                )
        );

        Ticket ticket = ticketRepository.save(
                new Ticket(order, category)
        );

        Long concertId = savedConcert.getId();
        String qrToken = ticket.getQrToken();

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready =
                new CountDownLatch(2);

        CountDownLatch start =
                new CountDownLatch(1);

        Callable<AttemptResult> attempt = () -> {
            ready.countDown();
            start.await();

            try {
                ticketValidationService.useTicket(
                        concertId,
                        qrToken
                );

                return AttemptResult.success();
            } catch (Throwable throwable) {
                return AttemptResult.failure(throwable);
            }
        };

        Future<AttemptResult> first =
                executor.submit(attempt);

        Future<AttemptResult> second =
                executor.submit(attempt);

        try {
            assertTrue(
                    ready.await(5, TimeUnit.SECONDS),
                    "Beide Scanner sollten startbereit sein"
            );

            start.countDown();

            List<AttemptResult> results = List.of(
                    first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS)
            );

            long successes = results.stream()
                    .filter(AttemptResult::successful)
                    .count();

            assertEquals(
                    1,
                    successes,
                    "Genau ein Scanner darf das Ticket entwerten"
            );

            AttemptResult failure = results.stream()
                    .filter(result -> !result.successful())
                    .findFirst()
                    .orElseThrow();

            assertInstanceOf(
                    TicketIsNotValidException.class,
                    failure.throwable()
            );

            Ticket finalTicket =
                    ticketRepository.findById(ticket.getId())
                            .orElseThrow();

            assertEquals(
                    TicketStatus.USED,
                    finalTicket.getStatus()
            );

        } finally {
            executor.shutdownNow();
        }
    }

    private record AttemptResult(
            boolean successful,
            Throwable throwable
    ) {

        static AttemptResult success() {
            return new AttemptResult(true, null);
        }

        static AttemptResult failure(Throwable throwable) {
            return new AttemptResult(false, throwable);
        }
    }
}