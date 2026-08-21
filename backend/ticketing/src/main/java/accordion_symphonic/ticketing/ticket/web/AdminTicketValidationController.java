package accordion_symphonic.ticketing.ticket.web;

import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.service.QrCodeService;
import accordion_symphonic.ticketing.ticket.service.TicketValidationService;
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
            @PathVariable("concertId") Long concertId,
            @PathVariable("qrToken") String qrToken
    ) {
        return ticketValidationService.validateTicket(concertId, qrToken);
    }

    @PatchMapping("/{qrToken}/use")
    public TicketResponse useTicket(
            @PathVariable("concertId") Long concertId,
            @PathVariable("qrToken") String qrToken
    ) {
        return ticketValidationService.useTicket(concertId, qrToken);
    }

    @GetMapping(
            value = "/{qrToken}/qr-code",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> getTicketQrCode(
            @PathVariable("concertId") Long concertId,
            @PathVariable("qrToken") String qrToken
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