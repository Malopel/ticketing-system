package accordion_symphonic.ticketing.ticket;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}/orders/{orderId}/tickets")
public class AdminTicketController {

    private final TicketService ticketService;

    public AdminTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponse> getTickets(@PathVariable Long concertId, @PathVariable Long orderId) {
        return this.ticketService.getTicketsByConcertIdAndOrderId(concertId, orderId);
    }
}
