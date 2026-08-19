package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderPaidEvent;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.OrderStatus;
import accordion_symphonic.ticketing.order.dto.OrderResponse;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.payment.exception.OrderCannotBePaidException;
import accordion_symphonic.ticketing.ticket.TicketService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentCompletionService {

    private final OrderRepository orderRepository;
    private final TicketService ticketService;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentCompletionService(
            OrderRepository orderRepository,
            TicketService ticketService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.ticketService = ticketService;
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

        order.markAsPaid();

        ticketService.createTicketsForOrder(order);

        eventPublisher.publishEvent(
                new OrderPaidEvent(
                        order.getConcert().getId(),
                        order.getId()
                )
        );

        return order;
    }
}