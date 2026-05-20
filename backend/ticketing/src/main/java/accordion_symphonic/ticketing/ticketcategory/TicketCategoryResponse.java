package accordion_symphonic.ticketing.ticketcategory;

import java.math.BigDecimal;

public record TicketCategoryResponse(
        Long id,
        String name,
        BigDecimal price,
        int capacity
) {
    public static TicketCategoryResponse fromEntity(TicketCategory category) {
        return new TicketCategoryResponse(
                category.getId(),
                category.getName(),
                category.getPrice(),
                category.getCapacity()
        );
    }
}