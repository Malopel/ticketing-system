package accordion_symphonic.ticketing.ticket.service;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
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
            DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm 'Uhr'");

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
            writeText(contentStream, "Accordion Symphonic", titleFont, 22, 70, 760);
            writeText(contentStream, "Eintrittskarte", boldFont, 16, 70, 730);

            writeText(contentStream, "Konzert:", boldFont, 12, 70, 680);
            writeText(contentStream, safeText(concert.getTitle()), normalFont, 12, 170, 680);

            writeText(contentStream, "Datum:", boldFont, 12, 70, 655);
            writeText(
                    contentStream,
                    concert.getStartTime().format(DATE_TIME_FORMATTER),
                    normalFont,
                    12,
                    170,
                    655
            );

            writeText(contentStream, "Ort:", boldFont, 12, 70, 630);
            writeText(contentStream, safeText(concert.getLocation()), normalFont, 12, 170, 630);

            writeText(contentStream, "Kategorie:", boldFont, 12, 70, 590);
            writeText(contentStream, safeText(ticket.ticketCategoryName()), normalFont, 12, 170, 590);

            writeText(contentStream, "Bestellnummer:", boldFont, 12, 70, 565);
            writeText(contentStream, order.getPublicId(), normalFont, 12, 170, 565);

            contentStream.drawImage(qrCodeImage, 350, 545, 170, 170);

            writeText(contentStream, "Einlasshinweis:", boldFont, 11, 70, 400);
            writeText(
                    contentStream,
                    "Dieses Ticket ist nur einmal gültig und wird beim Einlass gescannt.",
                    normalFont,
                    10,
                    70,
                    380
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

    private String safeText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\n", " ")
                .replace("\r", " ");
    }
}