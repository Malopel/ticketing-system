package accordion_symphonic.ticketing.order;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/concerts/{concertId}/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long concertId, @PathVariable Long orderId) {
        return orderService.getOrderByConcertIdAndId(concertId, orderId);
    }

    @PostMapping
    public OrderResponse createOrder(
            @PathVariable Long concertId,
            @Valid @RequestBody OrderRequest order
    ) {
        return orderService.createOrder(concertId, order);
    }

    @PatchMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long concertId,  @PathVariable Long orderId) {
        return orderService.cancelOrder(concertId, orderId);
    }
}
