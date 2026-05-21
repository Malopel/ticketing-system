package accordion_symphonic.ticketing.ticket;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/concerts/{concertId}/orders/{orderId}/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponse> getTickets(@PathVariable Long concertId, @PathVariable Long orderId) {
        return this.ticketService.getTicketsByConcertIdAndOrderId(concertId, orderId);
    }
}
