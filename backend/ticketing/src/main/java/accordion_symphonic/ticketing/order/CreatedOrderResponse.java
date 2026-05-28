package accordion_symphonic.ticketing.order;

public record CreatedOrderResponse(OrderResponse order, String accessToken) {

    public static CreatedOrderResponse fromEntity(Order order, String accessToken) {
        return new CreatedOrderResponse(
                OrderResponse.fromEntity(order),
                accessToken
        );
    }
}
