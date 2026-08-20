package accordion_symphonic.ticketing.payment.service;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.payment.PaymentEvent;
import accordion_symphonic.ticketing.payment.PaymentEventRepository;
import accordion_symphonic.ticketing.payment.dto.PaymentWebhookRequest;
import accordion_symphonic.ticketing.payment.exception.InvalidPaymentWebhookSignatureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentWebhookService {

    private final PaymentWebhookSignatureService signatureService;
    private final PaymentEventRepository paymentEventRepository;
    private final PaymentCompletionService paymentCompletionService;

    public PaymentWebhookService(
            PaymentWebhookSignatureService signatureService,
            PaymentEventRepository paymentEventRepository,
            PaymentCompletionService paymentCompletionService
    ) {
        this.signatureService = signatureService;
        this.paymentEventRepository = paymentEventRepository;
        this.paymentCompletionService = paymentCompletionService;
    }

    @Transactional
    public void processWebhook(
            String rawRequestBody,
            String providedSignature,
            PaymentWebhookRequest request
    ) {
        if (!signatureService.isValid(rawRequestBody, providedSignature)) {
            throw new InvalidPaymentWebhookSignatureException();
        }

        if (paymentEventRepository.existsByEventId(request.eventId())) {
            return;
        }

        Order paidOrder = paymentCompletionService.completePayment(
                request.orderId()
        );

        LocalDateTime now = LocalDateTime.now();

        PaymentEvent paymentEvent = new PaymentEvent(
                request.eventId(),
                paidOrder,
                request.status(),
                now,
                now
        );

        paymentEventRepository.save(paymentEvent);
    }
}