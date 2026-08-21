package accordion_symphonic.ticketing.payment.web;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.dto.OrderResponse;
import accordion_symphonic.ticketing.payment.service.PaymentCompletionService;
import accordion_symphonic.ticketing.payment.provider.FakePaymentProvider;
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
    private final PaymentCompletionService paymentCompletionService;

    public FakePaymentController(
            FakePaymentProvider fakePaymentProvider,
            PaymentCompletionService paymentCompletionService
    ) {
        this.fakePaymentProvider = fakePaymentProvider;
        this.paymentCompletionService = paymentCompletionService;
    }

    @PostMapping("/{providerPaymentId}/complete")
    public OrderResponse completePayment(
            @PathVariable("providerPaymentId") String providerPaymentId
    ) {
        Long orderId = fakePaymentProvider
                .findOrderId(providerPaymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fake payment not found"
                ));

        Order paidOrder =
                paymentCompletionService.completePayment(orderId);

        return OrderResponse.fromEntity(paidOrder);
    }
}