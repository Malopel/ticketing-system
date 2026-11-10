package accordion_symphonic.ticketing.ticket.dto;

import accordion_symphonic.ticketing.order.dto.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TicketBatchRequest (
        @NotEmpty
        List<@Valid OrderItemRequest> items
) { }