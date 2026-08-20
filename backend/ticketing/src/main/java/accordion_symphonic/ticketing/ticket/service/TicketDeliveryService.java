package accordion_symphonic.ticketing.ticket.service;

import accordion_symphonic.ticketing.mail.TicketEmailService;
import accordion_symphonic.ticketing.order.exception.OrderHasNoTicketsException;
import accordion_symphonic.ticketing.ticket.dto.TicketOrderData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketDeliveryService {

    private final TicketQueryService ticketQueryService;
    private final TicketPdfService ticketPdfService;
    private final TicketEmailService ticketEmailService;

    public TicketDeliveryService(
            TicketQueryService ticketQueryService,
            TicketPdfService ticketPdfService,
            TicketEmailService ticketEmailService
    ) {
        this.ticketQueryService = ticketQueryService;
        this.ticketPdfService = ticketPdfService;
        this.ticketEmailService = ticketEmailService;
    }

    @Transactional(readOnly = true)
    public byte[] createTicketPdfForOrder(
            Long concertId,
            Long orderId
    ) {
        TicketOrderData data =
                getTicketOrderData(concertId, orderId);

        return ticketPdfService.createTicketPdf(
                data.order(),
                data.tickets()
        );
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public void sendTicketEmail(
            Long concertId,
            Long orderId
    ) {
        TicketOrderData data =
                getTicketOrderData(concertId, orderId);

        ticketEmailService.sendEmail(
                data.order(),
                data.tickets()
        );
    }

    private TicketOrderData getTicketOrderData(
            Long concertId,
            Long orderId
    ) {
        TicketOrderData data =
                ticketQueryService.getTicketOrderData(
                        concertId,
                        orderId
                );

        if (data.tickets().isEmpty()) {
            throw new OrderHasNoTicketsException(orderId);
        }

        return data;
    }
}