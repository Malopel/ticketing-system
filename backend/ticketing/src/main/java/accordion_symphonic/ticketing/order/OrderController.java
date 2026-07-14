package accordion_symphonic.ticketing.order;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/concerts/{concertId}/orders")
public class OrderController {

    private static final String ORDER_ACCESS_TOKEN_HEADER = "X-Order-Access-Token";

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @PathVariable Long concertId,
            @PathVariable Long orderId,
            @RequestHeader(
                    name = ORDER_ACCESS_TOKEN_HEADER,
                    required = false
            ) String accessToken
    ) {
        return orderService.getCustomerOrder(concertId, orderId, accessToken);
    }

    @PostMapping
    public CreatedOrderResponse createOrder(
            @PathVariable Long concertId,
            @Valid @RequestBody OrderRequest order
    ) {
        return orderService.createOrder(concertId, order);
    }

    @PatchMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable Long concertId,
            @PathVariable Long orderId,
            @RequestHeader(
                    name = ORDER_ACCESS_TOKEN_HEADER,
                    required = false
            ) String accessToken
    ) {
        return orderService.cancelOrder(concertId, orderId, accessToken);
    }
}
