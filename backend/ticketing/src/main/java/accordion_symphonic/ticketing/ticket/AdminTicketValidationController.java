package accordion_symphonic.ticketing.ticket;

import org.springframework.web.bind.annotation.*;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

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

    @GetMapping(value = "/{qrToken}/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getTicketQrCode(
            @PathVariable Long concertId,
            @PathVariable String qrToken
    ) {
        byte[] qrCode = ticketService.generateQrCodePng(concertId, qrToken);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(qrCode);
    }
}