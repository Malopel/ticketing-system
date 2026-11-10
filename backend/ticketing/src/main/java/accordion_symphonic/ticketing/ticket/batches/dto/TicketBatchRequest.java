package accordion_symphonic.ticketing.ticket.batches.dto;

import accordion_symphonic.ticketing.order.dto.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TicketBatchRequest (
        @NotBlank
        String label,

        @NotEmpty
        List<@Valid OrderItemRequest> items
) {}