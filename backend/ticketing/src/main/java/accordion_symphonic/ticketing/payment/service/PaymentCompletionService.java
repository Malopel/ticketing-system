package accordion_symphonic.ticketing.payment.service;

import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderPaidEvent;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.OrderStatus;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.payment.exception.OrderCannotBePaidException;
import accordion_symphonic.ticketing.ticket.service.TicketCreationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentCompletionService {

    private final OrderRepository orderRepository;
    private final TicketCreationService ticketCreationService;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentCompletionService(
            OrderRepository orderRepository,
            TicketCreationService ticketCreationService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.ticketCreationService = ticketCreationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(
            noRollbackFor = OrderCannotBePaidException.class
    )
    public Order completePayment(Long orderId) {
        Order order = orderRepository
                .findByIdForUpdate(orderId)
                .orElseThrow(
                        () -> new OrderNotFoundException(orderId)
                );

        if (order.getStatus() == OrderStatus.PAID) {
            return order;
        }

        if (order.shouldExpirePayment()) {
            order.expire();

            throw new OrderCannotBePaidException(orderId);
        }

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new OrderCannotBePaidException(orderId);
        }

        if (order.getConcert().getStatus()
                != ConcertStatus.PUBLISHED) {
            throw new OrderCannotBePaidException(orderId);
        }

        order.markAsPaid();

        ticketCreationService.ensureTicketsCreatedForOrder(order);

        eventPublisher.publishEvent(
                new OrderPaidEvent(
                        order.getConcert().getId(),
                        order.getId()
                )
        );

        return order;
    }
}