package accordion_symphonic.ticketing.mail;

import accordion_symphonic.ticketing.order.OrderPaidEvent;
import accordion_symphonic.ticketing.ticket.service.TicketDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderPaidEmailListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OrderPaidEmailListener.class);

    private final TicketDeliveryService ticketDeliveryService;

    public OrderPaidEmailListener(TicketDeliveryService ticketDeliveryService) {
        this.ticketDeliveryService = ticketDeliveryService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderPaid(OrderPaidEvent event) {
        try {
            ticketDeliveryService.resendTicketEmail(
                    event.concertId(),
                    event.orderId()
            );
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Ticket-E-Mail für Order {} konnte nach erfolgreicher Zahlung nicht versendet werden.",
                    event.orderId(),
                    exception
            );
        }
    }
}