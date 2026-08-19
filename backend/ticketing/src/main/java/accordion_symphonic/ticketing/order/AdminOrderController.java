package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.order.dto.OrderResponse;
import accordion_symphonic.ticketing.ticket.TicketDeliveryService;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}/orders")
public class AdminOrderController {

    private final OrderService orderService;

    private final TicketDeliveryService ticketDeliveryService;

    public AdminOrderController(OrderService orderService, TicketDeliveryService ticketDeliveryService) {
        this.orderService = orderService;
        this.ticketDeliveryService = ticketDeliveryService;
    }

    @GetMapping
    public List<OrderResponse> getOrdersForConcert(@PathVariable Long concertId) {
        return orderService.getOrdersForConcert(concertId);
    }

    @GetMapping(value = "/{orderId}/tickets/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadTicketPdf(
            @PathVariable Long concertId,
            @PathVariable Long orderId
    ) {
        byte[] pdf = ticketDeliveryService.createTicketPdfForOrder(concertId, orderId);

        String filename = "tickets-order-" + orderId + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(filename)
                                .build()
                                .toString()
                )
                .body(pdf);
    }

    @PostMapping("/{orderId}/tickets/resend-email")
    public ResponseEntity<Void> resendTicketEmail(
            @PathVariable Long concertId,
            @PathVariable Long orderId
    ) {
        ticketDeliveryService.resendTicketEmail(concertId, orderId);

        return ResponseEntity.noContent().build();
    }
}
