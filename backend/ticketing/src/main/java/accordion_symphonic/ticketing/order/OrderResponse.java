package accordion_symphonic.ticketing.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long concertId,
        String concertTitle,
        String customerEmail,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime paidAt,
        List<OrderItemResponse> items
) {
    public static  OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getConcert().getId(),
                order.getConcert().getTitle(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getExpiresAt(),
                order.getPaidAt(),
                order.getItems().stream().map(OrderItemResponse::fromEntity).toList()
        );
    }
}
