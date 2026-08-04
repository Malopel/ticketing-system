package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TicketPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final QrCodeService qrCodeService;

    public TicketPdfService(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    public byte[] createTicketPdf(Order order, List<TicketResponse> tickets) {
        try (PDDocument document = new PDDocument()) {
            for (TicketResponse ticket : tickets) {
                addTicketPage(document, order, ticket);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);

            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Ticket-PDF konnte nicht erzeugt werden.", exception);
        }
    }

    private void addTicketPage(
            PDDocument document,
            Order order,
            TicketResponse ticket
    ) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        Concert concert = order.getConcert();

        PDType1Font titleFont =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font boldFont =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        byte[] qrCodePng = qrCodeService.generateQrCodePng(ticket.qrToken());
        PDImageXObject qrCodeImage =
                PDImageXObject.createFromByteArray(document, qrCodePng, "ticket-qr-code");

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            writeText(contentStream, "Accordion Symphonic Karlsruhe", titleFont, 22, 70, 760);
            writeText(contentStream, "Eintrittskarte", boldFont, 16, 70, 730);

            writeText(contentStream, "Konzert:", boldFont, 12, 70, 680);
            writeText(contentStream, safeText(concert.getTitle()), normalFont, 12, 170, 680);

            writeText(contentStream, "Datum:", boldFont, 12, 70, 655);
            writeText(contentStream, concert.getStartTime().format(DATE_TIME_FORMATTER), normalFont, 12, 170, 655);

            writeText(contentStream, "Ort:", boldFont, 12, 70, 630);
            writeText(contentStream, safeText(concert.getLocation()), normalFont, 12, 170, 630);

            writeText(contentStream, "Kategorie:", boldFont, 12, 70, 580);
            writeText(contentStream, safeText(ticket.ticketCategoryName()), normalFont, 12, 170, 580);

            writeText(contentStream, "Ticket-ID:", boldFont, 12, 70, 555);
            writeText(contentStream, String.valueOf(ticket.id()), normalFont, 12, 170, 555);

            writeText(contentStream, "Order-ID:", boldFont, 12, 70, 530);
            writeText(contentStream, String.valueOf(ticket.orderId()), normalFont, 12, 170, 530);

            writeText(contentStream, "Status:", boldFont, 12, 70, 505);
            writeText(contentStream, formatTicketStatus(ticket.status()), normalFont, 12, 170, 505);

            contentStream.drawImage(qrCodeImage, 350, 520, 150, 150);

            writeText(contentStream, "QR-Code / Ticket-Code:", boldFont, 11, 70, 450);
            writeText(contentStream, ticket.qrToken(), normalFont, 9, 70, 430);

            writeText(contentStream, "Hinweis:", boldFont, 11, 70, 370);
            writeText(
                    contentStream,
                    "Dieses Ticket ist nur einmal gueltig. Beim Einlass zaehlt der Status im Ticketsystem.",
                    normalFont,
                    10,
                    70,
                    350
            );
        }
    }

    private void writeText(
            PDPageContentStream contentStream,
            String text,
            PDType1Font font,
            float fontSize,
            float x,
            float y
    ) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(safeText(text));
        contentStream.endText();
    }

    private String formatTicketStatus(TicketStatus status) {
        return switch (status) {
            case VALID -> "Gueltig";
            case USED -> "Bereits verwendet";
            case CANCELLED -> "Storniert";
        };
    }

    private String safeText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\n", " ")
                .replace("\r", " ");
    }
}