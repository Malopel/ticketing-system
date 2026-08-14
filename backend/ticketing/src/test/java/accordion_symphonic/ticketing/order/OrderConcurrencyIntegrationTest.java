package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.availability.exception.NotEnoughTicketsAvailableException;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.order.dto.CreatedOrderResponse;
import accordion_symphonic.ticketing.order.dto.OrderItemRequest;
import accordion_symphonic.ticketing.order.dto.OrderRequest;
import accordion_symphonic.ticketing.order.exception.OrderIsPaidOrExpiredException;
import accordion_symphonic.ticketing.payment.exception.OrderCannotBePaidException;
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
import java.util.concurrent.*;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password",
        "ticketing.payment.webhook-secret=test-webhook-secret",
        "ticketing.order.reservation-duration=PT30M",
        "ticketing.payment.provider=fake"
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
        Concert concert = new Concert(
                "Concurrency Konzert",
                "Test für parallele Bestellungen",
                LocalDateTime.now().plusDays(30),
                "Konzertsaal"
        );

        concert.publish();

        Concert savedConcert = concertRepository.save(concert);

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
                            savedConcert.getId(),
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

    @Test
    void cancelAndPaymentCannotBothWin() throws Exception {
        Concert concert = new Concert(
                "Race Condition Konzert",
                "Test",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        concert.publish();
        final Concert savedConcert = concertRepository.save(concert);

        TicketCategory category = new TicketCategory(
                "Normal",
                new BigDecimal("25.00"),
                100,
                savedConcert
        );

        category = ticketCategoryRepository.save(category);

        OrderRequest request = new OrderRequest(
                "test@example.de",
                List.of(
                        new OrderItemRequest(
                                category.getId(),
                                1
                        )
                )
        );

        CreatedOrderResponse createdOrder =
                orderService.createOrder(
                        savedConcert.getId(),
                        request
                );

        Long orderId = createdOrder.order().id();
        String accessToken = createdOrder.accessToken();

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready =
                new CountDownLatch(2);

        CountDownLatch start =
                new CountDownLatch(1);

        Future<Boolean> cancelFuture =
                executor.submit(() -> {
                    ready.countDown();

                    start.await();

                    try {
                        orderService.cancelOrder(
                                savedConcert.getId(),
                                orderId,
                                accessToken
                        );

                        return true;
                    } catch (OrderIsPaidOrExpiredException exception) {
                        return false;
                    }
                });

        Future<Boolean> paymentFuture =
                executor.submit(() -> {
                    ready.countDown();

                    start.await();

                    try {
                        orderService.markOrderPaidFromPayment(
                                orderId
                        );

                        return true;
                    } catch (OrderCannotBePaidException exception) {
                        return false;
                    }
                });

        assertTrue(
                ready.await(5, TimeUnit.SECONDS),
                "Beide Threads sollten startbereit sein"
        );

        start.countDown();

        boolean cancelSucceeded =
                cancelFuture.get(10, TimeUnit.SECONDS);

        boolean paymentSucceeded =
                paymentFuture.get(10, TimeUnit.SECONDS);

        executor.shutdownNow();

        assertNotEquals(
                cancelSucceeded,
                paymentSucceeded,
                "Genau eine Operation muss erfolgreich sein"
        );

        Order finalOrder = orderRepository
                .findById(orderId)
                .orElseThrow();

        if (cancelSucceeded) {
            assertEquals(
                    OrderStatus.CANCELLED,
                    finalOrder.getStatus()
            );
        }

        if (paymentSucceeded) {
            assertEquals(
                    OrderStatus.PAID,
                    finalOrder.getStatus()
            );
        }
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