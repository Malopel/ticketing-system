package accordion_symphonic.ticketing.ticket.batches;

import accordion_symphonic.ticketing.ticket.batches.dto.TicketBatchRequest;
import accordion_symphonic.ticketing.ticket.batches.dto.TicketBatchResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}/print-ticket-batches")
public class AdminTicketBatchController {

    private final PrintedTicketBatchService printedTicketBatchService;

    public AdminTicketBatchController(PrintedTicketBatchService printedTicketBatchService) {
        this.printedTicketBatchService = printedTicketBatchService;
    }

    @PostMapping
    public TicketBatchResponse createPrintableBatch(
            @PathVariable("concertId") Long concertId,
            @Valid @RequestBody TicketBatchRequest ticketBatchRequest
    ) {
        return this.printedTicketBatchService.createPrintableBatch(concertId, ticketBatchRequest);
    }

    @GetMapping
    public List<TicketBatchResponse> getAllBatches(
            @PathVariable("concertId") Long concertId
    ) {
        return this.printedTicketBatchService.getAllBatches(concertId);
    }

    @GetMapping(
            path = "/{batchId}/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> getPrintableBatch(
            @PathVariable("concertId") Long concertId,
            @PathVariable("batchId") Long batchId,
            @RequestParam TicketBatchPrintLayout layout
    ) {
        byte[] pdf =
                printedTicketBatchService.getPrintableBatch(
                        concertId,
                        batchId,
                        layout
                );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ticket-batch-" +
                                batchId +
                                "-" +
                                layout +
                                ".pdf\""
                )
                .body(pdf);
    }
}
