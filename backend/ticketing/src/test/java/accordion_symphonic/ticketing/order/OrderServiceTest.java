package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.availability.TicketAvailabilityService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.mail.TicketEmailService;
import accordion_symphonic.ticketing.payment.OrderCannotBePaidException;
import accordion_symphonic.ticketing.ticket.TicketPdfService;
import accordion_symphonic.ticketing.ticket.TicketResponse;
import accordion_symphonic.ticketing.ticket.TicketService;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketAvailabilityService ticketAvailabilityService;

    @Mock
    private OrderAccessTokenService orderAccessTokenService;

    @Mock
    private TicketEmailService ticketEmailService;

    private OrderService orderService;

    @Mock
    private TicketPdfService ticketPdfService;

    @Mock
    private ApplicationEventPublisher eventPublisher;


    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                ticketCategoryRepository,
                concertRepository,
                ticketService,
                ticketAvailabilityService,
                orderAccessTokenService,
                ticketEmailService,
                ticketPdfService,
                new OrderProperties(Duration.ofMinutes(30), 10),
                eventPublisher
        );
    }

    @Test
    void markOrderPaidFromPaymentCreatesTicketsAndPublishesEvent() {
        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        List<TicketResponse> tickets = List.of();

        when(orderRepository.findById(42L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        when(ticketService.createTicketsForOrder(order))
                .thenReturn(tickets);

        Order paidOrder = orderService.markOrderPaidFromPayment(42L);

        assertEquals(OrderStatus.PAID, paidOrder.getStatus());

        verify(orderRepository).save(order);
        verify(ticketService).createTicketsForOrder(order);

        verify(eventPublisher).publishEvent(
                any(OrderPaidEvent.class)
        );
    }

    @Test
    void markOrderPaidFromPaymentDoesNotCreateTicketsOrSendEmailWhenOrderIsAlreadyPaid() {
        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        order.markAsPaid();

        when(orderRepository.findById(42L))
                .thenReturn(Optional.of(order));

        Order paidOrder = orderService.markOrderPaidFromPayment(42L);

        assertEquals(OrderStatus.PAID, paidOrder.getStatus());

        verify(orderRepository).findById(42L);
        verify(orderRepository, never()).save(any());
        verify(ticketService, never()).createTicketsForOrder(any());

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void markOrderPaidFromPaymentRejectsExpiredOrderAndDoesNotCreateTicketsOrSendEmail() {
        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1)
        );

        when(orderRepository.findById(42L))
                .thenReturn(Optional.of(order));

        assertThrows(
                OrderCannotBePaidException.class,
                () -> orderService.markOrderPaidFromPayment(42L)
        );

        assertEquals(OrderStatus.EXPIRED, order.getStatus());

        verify(orderRepository, never()).save(order);
        verify(ticketService, never()).createTicketsForOrder(any());

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createOrderRejectsDuplicateTicketCategory() {
        Long concertId = 1L;
        Long ticketCategoryId = 7L;

        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        TicketCategory category = new TicketCategory(
                "Normalpreis",
                new BigDecimal("25.00"),
                100,
                concert
        );

        when(concertRepository.findByIdAndStatus(
                concertId,
                ConcertStatus.PUBLISHED
        ))
                .thenReturn(Optional.of(concert));

        when(orderAccessTokenService.generateToken())
                .thenReturn(new OrderAccessTokenService.GeneratedOrderAccessToken(
                        "plain-token",
                        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
                ));

        when(ticketCategoryRepository.findByIdAndConcertIdForUpdate(ticketCategoryId, concertId))
                .thenReturn(Optional.of(category));

        OrderRequest request = new OrderRequest(
                "kunde@example.com",
                List.of(
                        new OrderItemRequest(ticketCategoryId, 2),
                        new OrderItemRequest(ticketCategoryId, 3)
                )
        );

        assertThrows(
                DuplicateTicketCategoryException.class,
                () -> orderService.createOrder(concertId, request)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderRejectsMoreThanMaxTicketsPerOrder() {
        Long concertId = 1L;

        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        when(concertRepository.findByIdAndStatus(
                concertId,
                ConcertStatus.PUBLISHED
        ))
                .thenReturn(Optional.of(concert));

        OrderRequest request = new OrderRequest(
                "kunde@example.com",
                List.of(
                        new OrderItemRequest(7L, 6),
                        new OrderItemRequest(8L, 5)
                )
        );

        assertThrows(
                TooManyTicketsInOrderException.class,
                () -> orderService.createOrder(concertId, request)
        );

        verify(orderAccessTokenService, never()).generateToken();
        verify(ticketCategoryRepository, never()).findByIdAndConcertIdForUpdate(any(), any());
        verify(orderRepository, never()).save(any());
    }
}