package accordion_symphonic.ticketing.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull
        Long ticketCategoryId,

        @Min(1)
        int quantity
) {
}