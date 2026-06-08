package accordion_symphonic.ticketing.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api/webhooks/payments")
public class PaymentWebhookController {

    private static final String SIGNATURE_HEADER = "X-Webhook-Signature";

    private final PaymentWebhookService paymentWebhookService;
    private final JsonMapper jsonMapper;

    public PaymentWebhookController(
            PaymentWebhookService paymentWebhookService,
            JsonMapper jsonMapper
    ) {
        this.paymentWebhookService = paymentWebhookService;
        this.jsonMapper = jsonMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handlePaymentWebhook(
            @RequestHeader(
                    name = SIGNATURE_HEADER,
                    required = false
            ) String signature,
            @RequestBody String rawRequestBody
    ) {
        PaymentWebhookRequest request = jsonMapper.readValue(
                rawRequestBody,
                PaymentWebhookRequest.class
        );

        paymentWebhookService.processWebhook(
                rawRequestBody,
                signature,
                request
        );
    }
}