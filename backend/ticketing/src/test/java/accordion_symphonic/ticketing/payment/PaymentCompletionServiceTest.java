package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderPaidEvent;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.OrderStatus;
import accordion_symphonic.ticketing.payment.exception.OrderCannotBePaidException;
import accordion_symphonic.ticketing.payment.service.PaymentCompletionService;
import accordion_symphonic.ticketing.ticket.service.TicketCreationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCompletionServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TicketCreationService ticketCreationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PaymentCompletionService paymentCompletionService;

    @BeforeEach
    void setUp() {
        paymentCompletionService = new PaymentCompletionService(
                orderRepository,
                ticketCreationService,
                eventPublisher
        );
    }

    @Test
    void completePaymentCreatesTicketsAndPublishesEvent() {
        Order order = createPaymentPendingOrder();

        when(orderRepository.findByIdForUpdate(42L))
                .thenReturn(Optional.of(order));

        Order paidOrder =
                paymentCompletionService.completePayment(42L);

        assertEquals(
                OrderStatus.PAID,
                paidOrder.getStatus()
        );

        verify(ticketCreationService)
                .ensureTicketsCreatedForOrder(order);

        verify(eventPublisher).publishEvent(
                any(OrderPaidEvent.class)
        );
    }

    @Test
    void completePaymentIsIdempotentWhenOrderIsAlreadyPaid() {
        Order order = createPaymentPendingOrder();
        order.markAsPaid();

        when(orderRepository.findByIdForUpdate(42L))
                .thenReturn(Optional.of(order));

        Order paidOrder =
                paymentCompletionService.completePayment(42L);

        assertEquals(
                OrderStatus.PAID,
                paidOrder.getStatus()
        );

        verifyNoInteractions(ticketCreationService);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void completePaymentExpiresPaymentPendingOrderWhenPaymentExpired() {
        Order order = createReservedOrder();

        order.markAsPaymentPending(
                LocalDateTime.now().minusMinutes(1)
        );

        when(orderRepository.findByIdForUpdate(42L))
                .thenReturn(Optional.of(order));

        assertThrows(
                OrderCannotBePaidException.class,
                () -> paymentCompletionService.completePayment(42L)
        );

        assertEquals(
                OrderStatus.EXPIRED,
                order.getStatus()
        );

        verifyNoInteractions(ticketCreationService);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void completePaymentRejectsReservedOrder() {
        Order order = createReservedOrder();

        when(orderRepository.findByIdForUpdate(42L))
                .thenReturn(Optional.of(order));

        assertThrows(
                OrderCannotBePaidException.class,
                () -> paymentCompletionService.completePayment(42L)
        );

        assertEquals(
                OrderStatus.RESERVED,
                order.getStatus()
        );

        verifyNoInteractions(ticketCreationService);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void completePaymentRejectsCancelledConcert() {
        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        concert.publish();
        concert.cancel();

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        order.markAsPaymentPending(
                LocalDateTime.now().plusMinutes(20)
        );

        when(orderRepository.findByIdForUpdate(42L))
                .thenReturn(Optional.of(order));

        assertThrows(
                OrderCannotBePaidException.class,
                () -> paymentCompletionService.completePayment(42L)
        );

        assertEquals(
                OrderStatus.PAYMENT_PENDING,
                order.getStatus()
        );

        verifyNoInteractions(ticketCreationService);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void completePaymentRejectsArchivedConcert() {
        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        concert.publish();
        concert.archive();

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        order.markAsPaymentPending(
                LocalDateTime.now().plusMinutes(20)
        );

        when(orderRepository.findByIdForUpdate(42L))
                .thenReturn(Optional.of(order));

        assertThrows(
                OrderCannotBePaidException.class,
                () -> paymentCompletionService.completePayment(42L)
        );

        assertEquals(
                OrderStatus.PAYMENT_PENDING,
                order.getStatus()
        );

        verifyNoInteractions(ticketCreationService);
        verifyNoInteractions(eventPublisher);
    }

    private Order createPaymentPendingOrder() {
        Order order = createReservedOrder();

        order.markAsPaymentPending(
                LocalDateTime.now().plusMinutes(20)
        );

        return order;
    }

    private Order createReservedOrder() {
        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        concert.publish();

        return new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );
    }
}