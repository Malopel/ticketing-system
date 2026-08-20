package accordion_symphonic.ticketing.ticket.web;

import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.service.TicketQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}/orders/{orderId}/tickets")
public class AdminTicketController {

    private final TicketQueryService ticketQueryService;

    public AdminTicketController(TicketQueryService ticketQueryService) {
        this.ticketQueryService = ticketQueryService;
    }

    @GetMapping
    public List<TicketResponse> getTickets(@PathVariable Long concertId, @PathVariable Long orderId) {
        return this.ticketQueryService.getTicketsByConcertIdAndOrderId(concertId, orderId);
    }
}
