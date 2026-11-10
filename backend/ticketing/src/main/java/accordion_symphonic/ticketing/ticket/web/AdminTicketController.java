package accordion_symphonic.ticketing.ticket.web;

import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.service.TicketQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}")
public class AdminTicketController {

    private final TicketQueryService ticketQueryService;

    public AdminTicketController(
            TicketQueryService ticketQueryService
    ) {
        this.ticketQueryService = ticketQueryService;
    }

    @GetMapping("/orders/{orderId}/tickets")
    public List<TicketResponse> getTickets(
            @PathVariable("concertId") Long concertId,
            @PathVariable("orderId") Long orderId
    ) {
        return this.ticketQueryService.getTicketsByConcertIdAndOrderId(concertId, orderId);
    }
}
