package accordion_symphonic.ticketing.order;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> getOrdersForConcert(@PathVariable Long concertId) {
        return orderService.getOrdersForConcert(concertId);
    }

    @PatchMapping("/{orderId}/paid")
    public OrderResponse markOrderAsPaid(
            @PathVariable Long concertId,
            @PathVariable Long orderId
    ) {
        return orderService.markOrderAsPaid(concertId, orderId);
    }
}
