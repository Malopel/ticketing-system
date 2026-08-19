package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.availability.TicketAvailabilityService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.order.dto.OrderResponse;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerOrderServiceAccessTokenTest {

    private static final Long CONCERT_ID = 1L;
    private static final Long ORDER_ID = 42L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private TicketAvailabilityService ticketAvailabilityService;

    private CustomerOrderService customerOrderService;

    private Order order;
    private String validAccessToken;

    @BeforeEach
    void setUp() {
        OrderAccessTokenService orderAccessTokenService = new OrderAccessTokenService();

        customerOrderService = new CustomerOrderService(
                orderRepository,
                concertRepository,
                orderAccessTokenService
        );

        OrderAccessTokenService.GeneratedOrderAccessToken generatedToken =
                orderAccessTokenService.generateToken();

        validAccessToken = generatedToken.token();

        Concert concert = new Concert(
                "Test convert",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Konzertsaal"
        );

        order = new Order(
                concert,
                "kunde@example.com",
                generatedToken.tokenHash(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );

        when(concertRepository.existsById(CONCERT_ID))
                .thenReturn(true);
    }

    @Test
    void customerCanReadOrderWithCorrectToken() {
        when(orderRepository.findByIdAndConcertId(
            ORDER_ID,
            CONCERT_ID
        )).thenReturn(Optional.of(order));

        OrderResponse response = customerOrderService.getCustomerOrder(
                CONCERT_ID,
                ORDER_ID,
                validAccessToken
        );

        assertEquals("kunde@example.com", response.customerEmail());
        assertEquals(OrderStatus.RESERVED, response.status());
    }

    @Test
    void customerCannotReadOrderWithWrongToken() {
        when(orderRepository.findByIdAndConcertId(
                ORDER_ID,
                CONCERT_ID
        )).thenReturn(Optional.of(order));

        assertThrows(
                OrderNotFoundException.class,
                () -> customerOrderService.getCustomerOrder(
                        CONCERT_ID,
                        ORDER_ID,
                        "wrong-token"
                )
        );
    }

    @Test
    void customerCannotReadOrderWithoutToken() {
        when(orderRepository.findByIdAndConcertId(
                ORDER_ID,
                CONCERT_ID
        )).thenReturn(Optional.of(order));

        assertThrows(
                OrderNotFoundException.class,
                () -> customerOrderService.getCustomerOrder(
                        CONCERT_ID,
                        ORDER_ID,
                        null
                )
        );
    }

    @Test
    void customerCanCancelOrderWithCorrectToken() {
        when(orderRepository.findByIdAndConcertIdForUpdate(
            ORDER_ID,
            CONCERT_ID
        )).thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response = customerOrderService.cancelOrder(
                CONCERT_ID,
                ORDER_ID,
                validAccessToken
        );

        assertEquals(OrderStatus.CANCELLED, response.status());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(orderRepository).save(order);
    }

    @Test
    void customerCannotCancelOrderWithWrongToken() {
        when(orderRepository.findByIdAndConcertIdForUpdate(
                ORDER_ID,
                CONCERT_ID
        )).thenReturn(Optional.of(order));

        assertThrows(
                OrderNotFoundException.class,
                () -> customerOrderService.cancelOrder(
                        CONCERT_ID,
                        ORDER_ID,
                        "wrong-token"
                )
        );

        assertEquals(OrderStatus.RESERVED, order.getStatus());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void customerReadingExpiredOrderMarksItExpired() {
        when(orderRepository.findByIdAndConcertId(
                ORDER_ID,
                CONCERT_ID
        )).thenReturn(Optional.of(order));

        Order expiredOrder = new Order(
                order.getConcert(),
                "kunde@example.com",
                order.getAccessTokenHash(),
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1)
        );

        when(orderRepository.findByIdAndConcertId(ORDER_ID, CONCERT_ID))
                .thenReturn(Optional.of(expiredOrder));

        OrderResponse response = customerOrderService.getCustomerOrder(
                CONCERT_ID,
                ORDER_ID,
                validAccessToken
        );

        assertEquals(OrderStatus.EXPIRED, response.status());
        assertEquals(OrderStatus.EXPIRED, expiredOrder.getStatus());
    }
}