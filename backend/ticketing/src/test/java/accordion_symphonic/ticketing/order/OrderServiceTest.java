package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.availability.TicketAvailabilityService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.mail.TicketEmailService;
import accordion_symphonic.ticketing.payment.OrderCannotBePaidException;
import accordion_symphonic.ticketing.ticket.Ticket;
import accordion_symphonic.ticketing.ticket.TicketResponse;
import accordion_symphonic.ticketing.ticket.TicketService;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                new OrderProperties(Duration.ofMinutes(30))
        );
    }

    @Test
    void markOrderPaidFromPaymentCreatesTicketsAndSendsEmail() {
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
                LocalDateTime.now()
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
        verify(ticketEmailService).sendEmail(order, tickets);
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
                LocalDateTime.now()
        );

        order.markAsPaid();

        when(orderRepository.findById(42L))
                .thenReturn(Optional.of(order));

        Order paidOrder = orderService.markOrderPaidFromPayment(42L);

        assertEquals(OrderStatus.PAID, paidOrder.getStatus());

        verify(orderRepository).findById(42L);
        verify(orderRepository, never()).save(any());
        verify(ticketService, never()).createTicketsForOrder(any());
        verify(ticketEmailService, never()).sendEmail(any(), any());
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
        verify(ticketEmailService, never()).sendEmail(any(), any());
    }
}