package accordion_symphonic.ticketing.order.dto;

import accordion_symphonic.ticketing.order.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long ticketCategoryId,
        String ticketCategoryName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
    public static OrderItemResponse fromEntity(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getTicketCategory().getId(),
                orderItem.getTicketCategory().getName(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getTotalPrice()
        );
    }
}
