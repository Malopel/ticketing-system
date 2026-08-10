package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderResponse;
import accordion_symphonic.ticketing.order.OrderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/fake-payments")
@ConditionalOnProperty(
        name = "ticketing.payment.provider",
        havingValue = "fake"
)
public class FakePaymentController {

    private final FakePaymentProvider fakePaymentProvider;
    private final OrderService orderService;

    public FakePaymentController(
            FakePaymentProvider fakePaymentProvider,
            OrderService orderService
    ) {
        this.fakePaymentProvider = fakePaymentProvider;
        this.orderService = orderService;
    }

    @PostMapping("/{providerPaymentId}/complete")
    public OrderResponse completePayment(
            @PathVariable String providerPaymentId
    ) {
        Long orderId = fakePaymentProvider
                .findOrderId(providerPaymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fake payment not found"
                ));

        Order paidOrder =
                orderService.markOrderPaidFromPayment(orderId);

        return OrderResponse.fromEntity(paidOrder);
    }
}