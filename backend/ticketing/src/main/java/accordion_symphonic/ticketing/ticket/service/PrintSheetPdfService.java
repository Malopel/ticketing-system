package accordion_symphonic.ticketing.ticket.service;

import accordion_symphonic.ticketing.ticket.batches.TicketBatchPrintLayout;
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

    public byte[] createPrintSheet(List<byte[]> ticketPdfs, TicketBatchPrintLayout layout) {
        if (ticketPdfs.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one ticket PDF is required."
            );
        }

        return switch (layout) {
            case FOUR_UP -> createFourUp(ticketPdfs);
            case EIGHT_UP -> createEightUp(ticketPdfs);
            case ONE -> createOneUp(ticketPdfs);
        };
    }

    private byte[] createFourUp(
            List<byte[]> ticketPdfs
    ) {
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

                drawFourUpCutMarks(result, a4Page);
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

    private byte[] createEightUp(
            List<byte[]> ticketPdfs
    ) {
        try (
                PDDocument result = new PDDocument();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            for (
                    int i = 0;
                    i < ticketPdfs.size();
                    i += 8
            ) {
                PDPage a4Page =
                        new PDPage(PDRectangle.A4);

                result.addPage(a4Page);

                int ticketsOnPage =
                        Math.min(
                                8,
                                ticketPdfs.size() - i
                        );

                for (
                        int j = 0;
                        j < ticketsOnPage;
                        j++
                ) {
                    addLegacyTicketToEightUpSheet(
                            result,
                            a4Page,
                            ticketPdfs.get(i + j),
                            j
                    );
                }

                drawEightUpCutMarks(
                        result,
                        a4Page
                );
            }

            result.save(output);

            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Eight-up print sheet could not be generated.",
                    exception
            );
        }
    }

    private byte[] createOneUp(
            List<byte[]> ticketPdfs
    ) {
        try (
                PDDocument result = new PDDocument();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {
            LayerUtility layerUtility =
                    new LayerUtility(result);

            for (byte[] ticketPdfBytes : ticketPdfs) {
                try (PDDocument ticketDocument =
                             Loader.loadPDF(ticketPdfBytes)) {

                    PDPage page =
                            new PDPage(PDRectangle.A4);

                    result.addPage(page);

                    PDFormXObject ticketForm =
                            layerUtility.importPageAsForm(
                                    ticketDocument,
                                    0
                            );

                    try (PDPageContentStream stream =
                                 new PDPageContentStream(
                                         result,
                                         page
                                 )) {

                        stream.drawForm(ticketForm);
                    }
                }
            }

            result.save(output);
            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "One-up print sheet could not be generated.",
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

    private void drawFourUpCutMarks(
            PDDocument document,
            PDPage page
    ) throws IOException {

        float width =
                PDRectangle.A4.getWidth();

        float height =
                PDRectangle.A4.getHeight();

        float centerX =
                width / 2f;

        float centerY =
                height / 2f;

        float mark =
                22.7f;

        try (PDPageContentStream stream =
                     new PDPageContentStream(
                             document,
                             page,
                             PDPageContentStream.AppendMode.APPEND,
                             true
                     )) {

            stream.setLineWidth(0.4f);

            // Vertikaler Schnitt:
            // Marke oben
            stream.moveTo(centerX, height);
            stream.lineTo(centerX, height - mark);

            // Marke unten
            stream.moveTo(centerX, 0);
            stream.lineTo(centerX, mark);

            // Horizontaler Schnitt:
            // Marke links
            stream.moveTo(0, centerY);
            stream.lineTo(mark, centerY);

            // Marke rechts
            stream.moveTo(width, centerY);
            stream.lineTo(width - mark, centerY);

            stream.stroke();
        }
    }

    private void addLegacyTicketToEightUpSheet(
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

            float pageWidth =
                    PDRectangle.A4.getWidth();

            float pageHeight =
                    PDRectangle.A4.getHeight();

            float cellWidth =
                    pageWidth / 2f;

            float cellHeight =
                    pageHeight / 4f;

            float ticketWidth =
                    ticketForm.getBBox().getWidth();

            float ticketHeight =
                    ticketForm.getBBox().getHeight();

            // Wegen 90°-Drehung vertauscht
            float scaleX =
                    cellWidth / ticketHeight;

            float scaleY =
                    cellHeight / ticketWidth;

            float scale =
                    Math.min(scaleX, scaleY);

            int column =
                    position % 2;

            int row =
                    position / 2;

            float x =
                    column * cellWidth;

            float y =
                    pageHeight
                            - (row + 1) * cellHeight;

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

    private void drawEightUpCutMarks(
            PDDocument document,
            PDPage page
    ) throws IOException {

        float width =
                PDRectangle.A4.getWidth();

        float height =
                PDRectangle.A4.getHeight();

        float mark =
                22.7f;

        float centerX =
                width / 2f;

        try (PDPageContentStream stream =
                     new PDPageContentStream(
                             document,
                             page,
                             PDPageContentStream.AppendMode.APPEND,
                             true
                     )) {

            stream.setLineWidth(0.4f);

            // Vertikale Mitte
            stream.moveTo(centerX, height);
            stream.lineTo(
                    centerX,
                    height - mark
            );

            stream.moveTo(centerX, 0);
            stream.lineTo(
                    centerX,
                    mark
            );

            // 3 horizontale Schnittpositionen
            for (int row = 1; row < 4; row++) {

                float y =
                        height * row / 4f;

                stream.moveTo(
                        0,
                        y
                );

                stream.lineTo(
                        mark,
                        y
                );

                stream.moveTo(
                        width,
                        y
                );

                stream.lineTo(
                        width - mark,
                        y
                );
            }

            stream.stroke();
        }
    }
}