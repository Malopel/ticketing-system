package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.availability.NotEnoughTicketsAvailableException;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password",
        "ticketing.payment.webhook-secret=test-webhook-secret",
        "ticketing.order.reservation-duration=PT30M"
})
class OrderConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private OrderService orderService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void concurrentOrdersDoNotOversellTicketCategory() throws Exception {
        Concert concert = concertRepository.save(
                new Concert(
                        "Concurrency Konzert",
                        "Test für parallele Bestellungen",
                        LocalDateTime.now().plusDays(30),
                        "Konzertsaal"
                )
        );

        TicketCategory category = ticketCategoryRepository.save(
                new TicketCategory(
                        "Normalpreis",
                        new BigDecimal("25.00"),
                        1,
                        concert
                )
        );

        int attempts = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(attempts);
        CountDownLatch startSignal = new CountDownLatch(1);

        List<Future<OrderAttemptResult>> futures = new ArrayList<>();

        for (int i = 0; i < attempts; i++) {
            int customerNumber = i;

            Callable<OrderAttemptResult> task = () -> {
                startSignal.await();

                try {
                    orderService.createOrder(
                            concert.getId(),
                            new OrderRequest(
                                    "kunde" + customerNumber + "@example.com",
                                    List.of(
                                            new OrderItemRequest(
                                                    category.getId(),
                                                    1
                                            )
                                    )
                            )
                    );

                    return OrderAttemptResult.success();
                } catch (Throwable throwable) {
                    return OrderAttemptResult.failure(throwable);
                }
            };

            futures.add(executorService.submit(task));
        }

        startSignal.countDown();

        List<OrderAttemptResult> results = new ArrayList<>();

        for (Future<OrderAttemptResult> future : futures) {
            results.add(future.get());
        }

        executorService.shutdown();

        long successfulOrders = results.stream()
                .filter(OrderAttemptResult::successful)
                .count();

        long rejectedOrders = results.stream()
                .filter(result -> !result.successful())
                .count();

        assertEquals(1, successfulOrders);
        assertEquals(9, rejectedOrders);

        results.stream()
                .filter(result -> !result.successful())
                .forEach(result ->
                        assertInstanceOf(
                                NotEnoughTicketsAvailableException.class,
                                result.throwable()
                        )
                );

        assertEquals(1, orderRepository.findAll().size());
    }

    private record OrderAttemptResult(
            boolean successful,
            Throwable throwable
    ) {
        static OrderAttemptResult success() {
            return new OrderAttemptResult(true, null);
        }

        static OrderAttemptResult failure(Throwable throwable) {
            return new OrderAttemptResult(false, throwable);
        }
    }
}