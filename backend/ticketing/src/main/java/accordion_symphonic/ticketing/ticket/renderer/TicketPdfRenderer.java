package accordion_symphonic.ticketing.ticket.renderer;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;

import java.util.List;

public interface TicketPdfRenderer {

    boolean supports(Concert concert);

    byte[] createPdf(
            Order order,
            List<TicketResponse> tickets
    );
}