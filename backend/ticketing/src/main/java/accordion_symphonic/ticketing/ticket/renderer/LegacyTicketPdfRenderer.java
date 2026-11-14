package accordion_symphonic.ticketing.ticket.renderer;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.service.QrCodeService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LegacyTicketPdfRenderer implements TicketPdfRenderer{

    private final QrCodeService qrCodeService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm 'Uhr'");

    public LegacyTicketPdfRenderer(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @Override
    public boolean supports(Concert concert) {
        return concert.getTicketTemplateFile() == null
                || concert.getTicketTemplateFile().isBlank();
    }

    @Override
    public TicketPdfRendererType type() {
        return TicketPdfRendererType.LEGACY;
    }

    @Override
    public byte[] createPdf(
            Order order,
            List<TicketResponse> tickets
    ) {
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

        LocalDateTime entranceTime =
                concert.getStartTime().minusHours(1);

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
            writeText(contentStream, "ACCORDION SYMPHONIC", titleFont, 24, 70, 770);
            writeText(contentStream, "Eintrittskarte", boldFont, 15, 70, 742);

            writeText(contentStream, "Konzert:", boldFont, 10, 70, 690);
            writeText(contentStream, safeText(concert.getTitle()), normalFont, 14, 70, 670);

            writeText(contentStream, "Datum:", boldFont, 10, 70, 625);
            writeText(
                    contentStream,
                    concert.getStartTime().format(DATE_TIME_FORMATTER),
                    normalFont,
                    12,
                    70,
                    607
            );

            writeText(contentStream, "Einlass:", boldFont, 10, 250, 625);
            writeText(
                    contentStream,
                    entranceTime.format(DateTimeFormatter.ofPattern("HH:mm 'Uhr'")),
                    normalFont,
                    12,
                    250,
                    607
            );

            writeText(contentStream, "Ort:", boldFont, 10, 70, 565);

            writeWrappedText(
                    contentStream,
                    safeText(concert.getLocation()),
                    normalFont,
                    12,
                    70,
                    547,
                    240,
                    16
            );

            writeText(contentStream, "Kategorie:", boldFont, 10, 70, 500);
            writeText(contentStream, safeText(ticket.ticketCategoryName()), normalFont, 12, 70, 482);

            writeText(contentStream, "Bestellnummer:", boldFont, 10, 70, 440);
            writeText(contentStream, order.getPublicId(), normalFont, 11, 70, 422);

            contentStream.drawImage(qrCodeImage, 340, 420, 180, 180);

            writeText(
                    contentStream,
                    "Hinweise:",
                    boldFont,
                    11,
                    70,
                    350
            );

            writeText(
                    contentStream,
                    "Freie Platzwahl innerhalb der gebuchten Kategorie.",
                    normalFont,
                    10,
                    70,
                    330
            );

            writeText(
                    contentStream,
                    "Im Congress Center ist ausschließlich Kartenzahlung möglich.",
                    normalFont,
                    10,
                    70,
                    312
            );

            writeText(
                    contentStream,
                    "Bitte QR-Code am Einlass bereithalten.",
                    normalFont,
                    10,
                    70,
                    294
            );

            writeText(
                    contentStream,
                    "Dieses Ticket ist nur einmal gültig und wird beim Einlass gescannt.",
                    normalFont,
                    10,
                    70,
                    276
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

    private void writeWrappedText(
            PDPageContentStream contentStream,
            String text,
            PDType1Font font,
            float fontSize,
            float x,
            float y,
            float maxWidth,
            float lineHeight
    ) throws IOException {

        String[] words = safeText(text).split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String candidate =
                    line.isEmpty()
                            ? word
                            : line + " " + word;

            float width =
                    font.getStringWidth(candidate)
                            / 1000f
                            * fontSize;

            if (width > maxWidth && !line.isEmpty()) {
                writeText(
                        contentStream,
                        line.toString(),
                        font,
                        fontSize,
                        x,
                        y
                );

                y -= lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }

        if (!line.isEmpty()) {
            writeText(
                    contentStream,
                    line.toString(),
                    font,
                    fontSize,
                    x,
                    y
            );
        }
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
