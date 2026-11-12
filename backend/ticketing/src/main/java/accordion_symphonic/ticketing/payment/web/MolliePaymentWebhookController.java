package accordion_symphonic.ticketing.payment.web;

import accordion_symphonic.ticketing.payment.service.MolliePaymentWebhookService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/mollie")
@ConditionalOnProperty(
        name = "ticketing.payment.provider",
        havingValue = "mollie"
)
public class MolliePaymentWebhookController {

    private final MolliePaymentWebhookService webhookService;

    public MolliePaymentWebhookController(
            MolliePaymentWebhookService webhookService
    ) {
        this.webhookService = webhookService;
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleWebhook(
            @RequestParam("id") String paymentId
    ) {
        webhookService.processWebhook(paymentId);
    }
}
