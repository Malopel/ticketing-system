package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.order.dto.CreatedOrderResponse;
import accordion_symphonic.ticketing.order.dto.OrderRequest;
import accordion_symphonic.ticketing.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/concerts/{concertId}/orders")
public class OrderController {

    private static final String ORDER_ACCESS_TOKEN_HEADER = "X-Order-Access-Token";

    private final OrderService orderService;
    private final OrderCreationService orderCreationService;

    public OrderController(OrderService orderService, OrderCreationService orderCreationService) {
        this.orderService = orderService;
        this.orderCreationService = orderCreationService;
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
        return orderCreationService.createOrder(concertId, order);
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
