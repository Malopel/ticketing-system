package accordion_symphonic.ticketing.ticket.service;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.TicketPrintLayout;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.renderer.TicketPdfRenderer;
import accordion_symphonic.ticketing.ticket.renderer.TicketPdfRendererType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketPdfService {

    private final List<TicketPdfRenderer> renderers;

    public TicketPdfService(List<TicketPdfRenderer> renderers) {
        this.renderers = renderers;
    }

    public byte[] createTicketPdf(
            Order order,
            List<TicketResponse> tickets
    ) {
        Concert concert = order.getConcert();

        TicketPdfRenderer renderer = renderers.stream()
                .filter(candidate -> candidate.supports(concert))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No ticket PDF renderer available"
                        )
                );

        return renderer.createPdf(order, tickets);
    }

    public byte[] createTicketPdf(
            Order order,
            List<TicketResponse> tickets,
            TicketPrintLayout layout
    ) {
        TicketPdfRendererType rendererType = switch (layout) {
            case FOUR_UP -> TicketPdfRendererType.TEMPLATE;
            case EIGHT_UP -> TicketPdfRendererType.LEGACY;
        };

        return getRenderer(rendererType)
                .createPdf(order, tickets);
    }

    private TicketPdfRenderer getRenderer(
            TicketPdfRendererType type
    ) {
        return renderers.stream()
                .filter(renderer -> renderer.type() == type)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No renderer available for " + type
                        )
                );
    }
}