package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketPdfServiceTest {

    @Test
    void createTicketPdfReturnsPdfBytes() {
        QrCodeService qrCodeService = mock(QrCodeService.class);
        TicketPdfService ticketPdfService = new TicketPdfService(qrCodeService);

        Concert concert = new Concert(
                "Accordion Night",
                "Ein Testkonzert",
                LocalDateTime.now().plusDays(30),
                "Karlsruhe"
        );

        TicketCategory ticketCategory = new TicketCategory(
                "VIP",
                BigDecimal.valueOf(100),
                50,
                concert
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now().plusMinutes(30),
                LocalDateTime.now().plusDays(7)
        );

        Ticket ticket = new Ticket(order, ticketCategory);
        TicketResponse response = TicketResponse.fromEntity(ticket);

        when(qrCodeService.generateQrCodePng(ticket.getQrToken()))
                .thenReturn(new QrCodeService().generateQrCodePng(ticket.getQrToken()));

        byte[] pdf = ticketPdfService.createTicketPdf(order, List.of(response));

        assertTrue(pdf.length > 0);
        assertTrue(startsWithPdfHeader(pdf));
    }

    private boolean startsWithPdfHeader(byte[] bytes) {
        return bytes.length >= 4
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F';
    }
}