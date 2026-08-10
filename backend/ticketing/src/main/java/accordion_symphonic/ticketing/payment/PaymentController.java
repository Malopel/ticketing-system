package accordion_symphonic.ticketing.payment;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/concerts/{concertId}/orders/{orderId}/payment"
)
public class PaymentController {

    private static final String ORDER_ACCESS_TOKEN_HEADER =
            "X-Order-Access-Token";

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentSession createPayment(
            @PathVariable Long concertId,
            @PathVariable Long orderId,
            @RequestHeader(
                    name = ORDER_ACCESS_TOKEN_HEADER,
                    required = false
            ) String accessToken
    ) {
        return paymentService.createPayment(
                concertId,
                orderId,
                accessToken
        );
    }
}