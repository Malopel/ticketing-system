package accordion_symphonic.ticketing.ticketcategory;

import java.math.BigDecimal;

public record TicketCategoryResponse(
        Long id,
        String name,
        BigDecimal price,
        int capacity,
        int available
) {
    public static TicketCategoryResponse fromEntity(TicketCategory category, int available) {
        return new TicketCategoryResponse(
                category.getId(),
                category.getName(),
                category.getPrice(),
                category.getCapacity(),
                available
        );
    }
}