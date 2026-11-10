package accordion_symphonic.ticketing.ticket.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PrintSheetPdfService {

    public byte[] createPrintSheet(List<byte[]> ticketPdfs) {
        if (ticketPdfs.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one ticket PDF is required."
            );
        }

        try (
                PDDocument result = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {

            for (int i = 0; i < ticketPdfs.size(); i += 4) {
                PDPage a4Page = new PDPage(PDRectangle.A4);
                result.addPage(a4Page);

                int ticketsOnPage =
                        Math.min(4, ticketPdfs.size() - i);

                for (int j = 0; j < ticketsOnPage; j++) {
                    addTicketToSheet(
                            result,
                            a4Page,
                            ticketPdfs.get(i + j),
                            j
                    );
                }

                drawCutLines(result, a4Page);
            }

            result.save(output);
            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Print sheet PDF could not be generated.",
                    exception
            );
        }
    }

    private void addTicketToSheet(
            PDDocument targetDocument,
            PDPage targetPage,
            byte[] ticketPdfBytes,
            int position
    ) throws IOException {

        try (PDDocument ticketDocument =
                     Loader.loadPDF(ticketPdfBytes)) {

            LayerUtility layerUtility =
                    new LayerUtility(targetDocument);

            PDFormXObject ticketForm =
                    layerUtility.importPageAsForm(
                            ticketDocument,
                            0
                    );

            float cellWidth = PDRectangle.A4.getWidth() / 2f;
            float cellHeight = PDRectangle.A4.getHeight() / 2f;

            float ticketWidth = ticketForm.getBBox().getWidth();
            float ticketHeight = ticketForm.getBBox().getHeight();

            float scaleX = cellWidth / ticketHeight;
            float scaleY = cellHeight / ticketWidth;

            float scale = Math.min(scaleX, scaleY);

            float x;
            float y;

            switch (position) {
                case 0 -> {
                    x = 0;
                    y = cellHeight;
                }
                case 1 -> {
                    x = cellWidth;
                    y = cellHeight;
                }
                case 2 -> {
                    x = 0;
                    y = 0;
                }
                case 3 -> {
                    x = cellWidth;
                    y = 0;
                }
                default -> throw new IllegalArgumentException(
                        "Invalid ticket position: " + position
                );
            }

            try (PDPageContentStream stream =
                         new PDPageContentStream(
                                 targetDocument,
                                 targetPage,
                                 PDPageContentStream.AppendMode.APPEND,
                                 true
                         )) {

                stream.saveGraphicsState();

                stream.transform(
                        Matrix.getTranslateInstance(
                                x,
                                y + cellHeight
                        )
                );

                stream.transform(
                        Matrix.getRotateInstance(
                                Math.toRadians(-90),
                                0,
                                0
                        )
                );

                stream.transform(
                        Matrix.getScaleInstance(
                                scale,
                                scale
                        )
                );

                stream.drawForm(ticketForm);

                stream.restoreGraphicsState();
            }
        }
    }

    private void drawCutLines(
            PDDocument document,
            PDPage page
    ) throws IOException {

        float width = PDRectangle.A4.getWidth();
        float height = PDRectangle.A4.getHeight();

        try (PDPageContentStream stream =
                     new PDPageContentStream(
                             document,
                             page,
                             PDPageContentStream.AppendMode.APPEND,
                             true
                     )) {

            stream.setLineWidth(0.5f);

            // Vertikale Mitte
            stream.moveTo(width / 2f, 0);
            stream.lineTo(width / 2f, height);

            // Horizontale Mitte
            stream.moveTo(0, height / 2f);
            stream.lineTo(width, height / 2f);

            stream.stroke();
        }
    }
}