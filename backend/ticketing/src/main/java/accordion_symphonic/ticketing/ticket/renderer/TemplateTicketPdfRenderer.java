package accordion_symphonic.ticketing.ticket.renderer;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.service.QrCodeService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class TemplateTicketPdfRenderer implements TicketPdfRenderer {

    private final QrCodeService qrCodeService;

    public TemplateTicketPdfRenderer(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @Override
    public boolean supports(Concert concert) {
        return concert.getTicketTemplateFile() != null
                && !concert.getTicketTemplateFile().isBlank();
    }

    @Override
    public byte[] createPdf(Order order, List<TicketResponse> tickets) {
        try (
                PDDocument template = loadTemplate(order.getConcert().getTicketTemplateFile());
                PDDocument result = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            PDPage templatePage = template.getPage(0);

            for (TicketResponse ticket : tickets) {
                PDPage page = result.importPage(templatePage);

                addTicketContent(
                        result,
                        page,
                        order,
                        ticket
                );
            }

            result.save(output);

            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Ticket PDF could not be generated.",
                    exception
            );
        }
    }

    private PDDocument loadTemplate(String fileName) throws IOException {
        ClassPathResource resource =
                new ClassPathResource("pdf/tickets/" + fileName);

        if (!resource.exists()) {
            throw new IllegalArgumentException(
                    "Ticket template not found: " + fileName
            );
        }

        byte[] templateBytes;

        try (InputStream inputStream = resource.getInputStream()) {
            templateBytes = inputStream.readAllBytes();
        }

        return Loader.loadPDF(templateBytes);
    }

    private void addTicketContent(
            PDDocument document,
            PDPage page,
            Order order,
            TicketResponse ticket
    ) throws IOException {

        PDType1Font normalFont =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        PDType1Font boldFont =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        byte[] qrCodePng =
                qrCodeService.generateQrCodePng(ticket.qrToken());

        PDImageXObject qrCodeImage =
                PDImageXObject.createFromByteArray(
                        document,
                        qrCodePng,
                        "ticket-qr-code"
                );

        try (PDPageContentStream contentStream =
                     new PDPageContentStream(
                             document,
                             page,
                             PDPageContentStream.AppendMode.APPEND,
                             true,
                             true
                     )) {

            // Bestellnummer
            writeCenteredText(
                    contentStream,
                    order.getPublicId(),
                    normalFont,
                    7.4f,
                    375.9f,
                    193.7f
            );

            // Kategorie
            writeCenteredText(
                    contentStream,
                    safeText(ticket.ticketCategoryName()),
                    boldFont,
                    9.2f,
                    375.9f,
                    139.0f
            );

            // QR-Code
            contentStream.drawImage(
                    qrCodeImage,
                    344.7f,
                    213.4f,
                    64,
                    64
            );
        }
    }

    private void writeCenteredText(
            PDPageContentStream contentStream,
            String text,
            PDFont font,
            float fontSize,
            float centerX,
            float y
    ) throws IOException {
        String safe = safeText(text);

        float textWidth =
                font.getStringWidth(safe) / 1000f * fontSize;

        float x = centerX - textWidth / 2f;

        contentStream.setNonStrokingColor(
                25f / 255f,
                23f / 255f,
                23f / 255f
        );

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(safe);
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
