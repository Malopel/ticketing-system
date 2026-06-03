package accordion_symphonic.ticketing.order;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> getOrders(@PathVariable Long concertId) {
        return orderService.getOrderForConcert(concertId);
    }

    @PatchMapping("/{orderId}/paid")
    public OrderResponse markOrderAsPaid(
            @PathVariable Long concertId,
            @PathVariable Long orderId
    ) {
        return orderService.markOrderAsPaid(concertId, orderId);
    }
}
