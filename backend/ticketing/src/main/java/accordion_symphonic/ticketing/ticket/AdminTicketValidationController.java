package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}/tickets")
public class AdminTicketValidationController {

    private final TicketValidationService ticketValidationService;
    private final QrCodeService qrCodeService;

    public AdminTicketValidationController(
            TicketValidationService ticketValidationService,
            QrCodeService qrCodeService
    ) {
        this.ticketValidationService = ticketValidationService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/validate/{qrToken}")
    public TicketResponse validateTicket(
            @PathVariable Long concertId,
            @PathVariable String qrToken
    ) {
        return ticketValidationService.validateTicket(concertId, qrToken);
    }

    @PatchMapping("/{qrToken}/use")
    public TicketResponse useTicket(
            @PathVariable Long concertId,
            @PathVariable String qrToken
    ) {
        return ticketValidationService.useTicket(concertId, qrToken);
    }

    @GetMapping(
            value = "/{qrToken}/qr-code",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> getTicketQrCode(
            @PathVariable Long concertId,
            @PathVariable String qrToken
    ) {
        TicketResponse ticket =
                ticketValidationService.validateTicket(
                        concertId,
                        qrToken
                );

        byte[] qrCode =
                qrCodeService.generateQrCodePng(
                        ticket.qrToken()
                );

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(
                        CacheControl.maxAge(
                                1,
                                TimeUnit.HOURS
                        )
                )
                .body(qrCode);
    }
}