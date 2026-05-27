package accordion_symphonic.ticketing.ticket;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}/tickets")
public class AdminTicketValidationController {

    private final TicketService ticketService;

    public AdminTicketValidationController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/validate/{qrToken}")
    public TicketResponse validateTicket(
            @PathVariable Long concertId,
            @PathVariable String qrToken
    ) {
        return ticketService.validateTicket(concertId, qrToken);
    }

    @PatchMapping("/{qrToken}/use")
    public TicketResponse useTicket(
            @PathVariable Long concertId,
            @PathVariable String qrToken
    ) {
        return ticketService.useTicket(concertId, qrToken);
    }
}