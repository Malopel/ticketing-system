package accordion_symphonic.ticketing.ticket.dto;

import accordion_symphonic.ticketing.order.dto.OrderItemRequest;
import accordion_symphonic.ticketing.ticket.TicketPrintLayout;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TicketBatchRequest (
        @NotNull
        TicketPrintLayout layout,

        @NotEmpty
        List<@Valid OrderItemRequest> items
) {}