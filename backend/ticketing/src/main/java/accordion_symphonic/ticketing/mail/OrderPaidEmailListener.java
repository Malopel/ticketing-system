package accordion_symphonic.ticketing.mail;

import accordion_symphonic.ticketing.order.OrderPaidEvent;
import accordion_symphonic.ticketing.ticket.service.TicketDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class OrderPaidEmailListener {

    private final TicketDeliveryService ticketDeliveryService;

    public OrderPaidEmailListener(TicketDeliveryService ticketDeliveryService) {
        this.ticketDeliveryService = ticketDeliveryService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderPaid(OrderPaidEvent event) {
        try {
            ticketDeliveryService.sendTicketEmail(
                    event.concertId(),
                    event.orderId()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Ticket-E-Mail für Order {} konnte nach erfolgreicher Zahlung nicht versendet werden.",
                    event.orderId(),
                    exception
            );
        }
    }
}