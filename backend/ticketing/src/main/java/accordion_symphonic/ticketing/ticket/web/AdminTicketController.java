package accordion_symphonic.ticketing.ticket.web;

import accordion_symphonic.ticketing.ticket.dto.TicketBatchRequest;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.service.PrintedTicketBatchService;
import accordion_symphonic.ticketing.ticket.service.TicketQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}")
public class AdminTicketController {

    private final TicketQueryService ticketQueryService;
    private final PrintedTicketBatchService printedTicketBatchService;

    public AdminTicketController(
            TicketQueryService ticketQueryService,
            PrintedTicketBatchService printedTicketBatchService
    ) {
        this.ticketQueryService = ticketQueryService;
        this.printedTicketBatchService = printedTicketBatchService;
    }

    @GetMapping("/orders/{orderId}/tickets")
    public List<TicketResponse> getTickets(
            @PathVariable("concertId") Long concertId,
            @PathVariable("orderId") Long orderId
    ) {
        return this.ticketQueryService.getTicketsByConcertIdAndOrderId(concertId, orderId);
    }

    @PostMapping(
            value = "/print-tickets",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> createPrintableBatch(
            @PathVariable("concertId") Long concertId,
            @Valid @RequestBody TicketBatchRequest request
    ) {
        byte[] pdf =
                printedTicketBatchService.createPrintableBatch(
                        concertId,
                        request
                );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"tickets-" +
                                concertId +
                                ".pdf\""
                )
                .body(pdf);
    }
}
