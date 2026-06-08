package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentWebhookService {

    private final PaymentWebhookSignatureService signatureService;
    private final PaymentEventRepository paymentEventRepository;
    private final OrderService orderService;

    public PaymentWebhookService(
            PaymentWebhookSignatureService signatureService,
            PaymentEventRepository paymentEventRepository,
            OrderService orderService
    ) {
        this.signatureService = signatureService;
        this.paymentEventRepository = paymentEventRepository;
        this.orderService = orderService;
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

        if (request.status() != PaymentStatus.PAID) {
            throw new UnsupportedPaymentStatusException(request.status());
        }

        Order paidOrder = orderService.markOrderPaidFromPayment(
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