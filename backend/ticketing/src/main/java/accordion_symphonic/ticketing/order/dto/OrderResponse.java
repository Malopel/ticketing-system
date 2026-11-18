package accordion_symphonic.ticketing.order.dto;

import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderStatus;
import accordion_symphonic.ticketing.order.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String publicId,
        Long concertId,
        ConcertStatus concertStatus,
        String concertTitle,
        String customerEmail,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime paymentExpiresAt,
        LocalDateTime paidAt,
        PaymentMethod paymentMethod,
        List<OrderItemResponse> items
) {
    public static  OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getPublicId(),
                order.getConcert().getId(),
                order.getConcert().getStatus(),
                order.getConcert().getTitle(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getExpiresAt(),
                order.getPaymentExpiresAt(),
                order.getPaidAt(),
                order.getPaymentMethod(),
                order.getItems().stream().map(OrderItemResponse::fromEntity).toList()
        );
    }
}
