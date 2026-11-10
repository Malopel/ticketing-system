package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.batches.TicketBatchPrintLayout;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.renderer.TemplateTicketPdfRenderer;
import accordion_symphonic.ticketing.ticket.service.PrintSheetPdfService;
import accordion_symphonic.ticketing.ticket.service.QrCodeService;
import accordion_symphonic.ticketing.ticket.service.TicketPdfService;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketPdfServiceTest {

    @Test
    void createTicketPdfReturnsPdfBytes() throws Exception {

        QrCodeService qrCodeService = mock(QrCodeService.class);

        TemplateTicketPdfRenderer renderer =
                new TemplateTicketPdfRenderer(qrCodeService);

        TicketPdfService ticketPdfService =
                new TicketPdfService(List.of(renderer));

        Concert concert = new Concert(
                "Accordion Night",
                "Ein Testkonzert",
                LocalDateTime.now().plusDays(30),
                "Karlsruhe"
        );

        concert.updateTicketTemplateFile("AS-HD-2027.pdf");

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
                LocalDateTime.now().plusDays(7),
                "AS-TEST-0001"
        );

        Ticket ticket = new Ticket(order, ticketCategory);
        TicketResponse response = TicketResponse.fromEntity(ticket);

        when(qrCodeService.generateQrCodePng(ticket.getQrToken()))
                .thenReturn(new QrCodeService().generateQrCodePng(ticket.getQrToken()));

        byte[] pdf = ticketPdfService.createTicketPdf(order, List.of(response));

        Files.write(
                Path.of("build/test-ticket.pdf"),
                pdf
        );

        assertTrue(pdf.length > 0);
        assertTrue(startsWithPdfHeader(pdf));
    }

    @Test
    void createsA4PrintSheetWithFourTickets() throws Exception {

        QrCodeService qrCodeService = mock(QrCodeService.class);

        TemplateTicketPdfRenderer renderer =
                new TemplateTicketPdfRenderer(qrCodeService);

        TicketPdfService ticketPdfService =
                new TicketPdfService(List.of(renderer));

        Concert concert = new Concert(
                "Accordion Night",
                "Ein Testkonzert",
                LocalDateTime.now().plusDays(30),
                "Karlsruhe"
        );

        concert.updateTicketTemplateFile("AS-HD-2027.pdf");

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
                LocalDateTime.now().plusDays(7),
                "AS-TEST-0001"
        );

        Ticket ticket = new Ticket(order, ticketCategory);
        TicketResponse response = TicketResponse.fromEntity(ticket);

        when(qrCodeService.generateQrCodePng(ticket.getQrToken()))
                .thenReturn(new QrCodeService().generateQrCodePng(ticket.getQrToken()));

        byte[] ticketPdf = ticketPdfService.createTicketPdf(order, List.of(response));

        PrintSheetPdfService service =
                new PrintSheetPdfService();

        byte[] printSheet = service.createPrintSheet(
                List.of(
                        ticketPdf,
                        ticketPdf,
                        ticketPdf,
                        ticketPdf
                ),
                TicketBatchPrintLayout.FOUR_UP
        );

        Files.write(
                Path.of("build/test-print-sheet.pdf"),
                printSheet
        );

        assertTrue(printSheet.length > 0);
    }

    private boolean startsWithPdfHeader(byte[] bytes) {
        return bytes.length >= 4
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F';
    }
}