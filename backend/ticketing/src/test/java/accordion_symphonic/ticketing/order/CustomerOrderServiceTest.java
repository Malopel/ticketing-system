package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.order.dto.OrderResponse;
import accordion_symphonic.ticketing.order.exception.OrderCannotBeCancelledException;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.order.service.CustomerOrderService;
import accordion_symphonic.ticketing.order.service.OrderAccessTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private OrderAccessTokenService orderAccessTokenService;

    private CustomerOrderService customerOrderService;

    @BeforeEach
    void setUp() {
        customerOrderService = new CustomerOrderService(
                orderRepository,
                concertRepository,
                orderAccessTokenService
        );
    }

    @Test
    void cancelOrderCancelsReservedOrder() {
        Long concertId = 1L;
        Long orderId = 42L;
        String accessToken = "plain-token";

        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "stored-token-hash",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        when(concertRepository.existsById(concertId))
                .thenReturn(true);

        when(orderRepository.findByIdAndConcertIdForUpdate(
                orderId,
                concertId
        ))
                .thenReturn(Optional.of(order));

        when(orderAccessTokenService.matches(
                accessToken,
                order.getAccessTokenHash()
        ))
                .thenReturn(true);

        OrderResponse response = customerOrderService.cancelOrder(
                concertId,
                orderId,
                accessToken
        );

        assertEquals(
                OrderStatus.CANCELLED,
                response.status()
        );

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus()
        );
    }

    @Test
    void cancelOrderRejectsWrongAccessToken() {
        Long concertId = 1L;
        Long orderId = 42L;
        String accessToken = "wrong-token";

        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "stored-token-hash",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        when(concertRepository.existsById(concertId))
                .thenReturn(true);

        when(orderRepository.findByIdAndConcertIdForUpdate(
                orderId,
                concertId
        ))
                .thenReturn(Optional.of(order));

        when(orderAccessTokenService.matches(
                accessToken,
                order.getAccessTokenHash()
        ))
                .thenReturn(false);

        assertThrows(
                OrderNotFoundException.class,
                () -> customerOrderService.cancelOrder(
                        concertId,
                        orderId,
                        accessToken
                )
        );

        assertEquals(
                OrderStatus.RESERVED,
                order.getStatus()
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrderRejectsPaidOrder() {
        Long concertId = 1L;
        Long orderId = 42L;
        String accessToken = "plain-token";

        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "stored-token-hash",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        order.markAsPaymentPending(
                LocalDateTime.now().plusMinutes(20)
        );
        order.markAsPaid();

        when(concertRepository.existsById(concertId))
                .thenReturn(true);

        when(orderRepository.findByIdAndConcertIdForUpdate(
                orderId,
                concertId
        ))
                .thenReturn(Optional.of(order));

        when(orderAccessTokenService.matches(
                accessToken,
                order.getAccessTokenHash()
        ))
                .thenReturn(true);

        assertThrows(
                OrderCannotBeCancelledException.class,
                () -> customerOrderService.cancelOrder(
                        concertId,
                        orderId,
                        accessToken
                )
        );

        assertEquals(
                OrderStatus.PAID,
                order.getStatus()
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrderExpiresAndRejectsExpiredOrder() {
        Long concertId = 1L;
        Long orderId = 42L;
        String accessToken = "plain-token";

        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "stored-token-hash",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1)
        );

        when(concertRepository.existsById(concertId))
                .thenReturn(true);

        when(orderRepository.findByIdAndConcertIdForUpdate(
                orderId,
                concertId
        ))
                .thenReturn(Optional.of(order));

        when(orderAccessTokenService.matches(
                accessToken,
                order.getAccessTokenHash()
        ))
                .thenReturn(true);

        assertThrows(
                OrderCannotBeCancelledException.class,
                () -> customerOrderService.cancelOrder(
                        concertId,
                        orderId,
                        accessToken
                )
        );

        assertEquals(
                OrderStatus.EXPIRED,
                order.getStatus()
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrderIsIdempotentWhenAlreadyCancelled() {
        Long concertId = 1L;
        Long orderId = 42L;
        String accessToken = "plain-token";

        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "stored-token-hash",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        when(concertRepository.existsById(concertId))
                .thenReturn(true);

        when(orderRepository.findByIdAndConcertIdForUpdate(
                orderId,
                concertId
        ))
                .thenReturn(Optional.of(order));

        when(orderAccessTokenService.matches(
                accessToken,
                order.getAccessTokenHash()
        ))
                .thenReturn(true);

        customerOrderService.cancelOrder(
                concertId,
                orderId,
                accessToken
        );

        OrderResponse secondResponse =
                customerOrderService.cancelOrder(
                        concertId,
                        orderId,
                        accessToken
                );

        assertEquals(
                OrderStatus.CANCELLED,
                secondResponse.status()
        );

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus()
        );
    }
}