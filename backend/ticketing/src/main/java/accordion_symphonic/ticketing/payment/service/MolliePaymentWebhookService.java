package accordion_symphonic.ticketing.payment.service;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.payment.PaymentEvent;
import accordion_symphonic.ticketing.payment.PaymentEventRepository;
import accordion_symphonic.ticketing.payment.PaymentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@ConditionalOnProperty(
        name = "ticketing.payment.provider",
        havingValue = "mollie"
)
public class MolliePaymentWebhookService {

    private final RestClient restClient;
    private final OrderRepository orderRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final PaymentCompletionService paymentCompletionService;

    public MolliePaymentWebhookService(
            @Value("${mollie.api-key}") String apiKey,
            OrderRepository orderRepository,
            PaymentEventRepository paymentEventRepository,
            PaymentCompletionService paymentCompletionService
    ) {
        this.orderRepository = orderRepository;
        this.paymentEventRepository = paymentEventRepository;
        this.paymentCompletionService = paymentCompletionService;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.mollie.com/v2")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey
                )
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    @Transactional
    public void processWebhook(String paymentId) {
        MolliePaymentResponse payment = restClient.get()
                .uri("/payments/{id}", paymentId)
                .retrieve()
                .body(MolliePaymentResponse.class);

        if (payment == null) {
            throw new IllegalStateException(
                    "Mollie payment response was empty"
            );
        }

        if (!paymentId.equals(payment.id())) {
            throw new IllegalStateException(
                    "Mollie payment ID does not match webhook payment ID"
            );
        }

        if (!"paid".equals(payment.status())) {
            return;
        }

        if (payment.metadata() == null
                || payment.metadata().orderId() == null) {
            throw new IllegalStateException(
                    "Mollie payment has no orderId metadata"
            );
        }

        if (payment.amount() == null) {
            throw new IllegalStateException(
                    "Mollie payment has no amount"
            );
        }

        Long orderId = payment.metadata().orderId();

        Order order = orderRepository
                .findByIdForUpdate(orderId)
                .orElseThrow(
                        () -> new OrderNotFoundException(orderId)
                );

        if (!"EUR".equals(payment.amount().currency())) {
            throw new IllegalStateException(
                    "Unexpected Mollie payment currency: "
                            + payment.amount().currency()
            );
        }

        BigDecimal paidAmount =
                new BigDecimal(payment.amount().value());

        BigDecimal expectedAmount =
                order.getTotalAmount();

        if (paidAmount.compareTo(expectedAmount) != 0) {
            throw new IllegalStateException(
                    "Mollie payment amount does not match order amount"
            );
        }

        String eventId = "mollie:" + payment.id();

        if (paymentEventRepository.existsByEventId(eventId)) {
            return;
        }

        Order paidOrder =
                paymentCompletionService.completePayment(orderId);

        LocalDateTime now = LocalDateTime.now();

        PaymentEvent paymentEvent = new PaymentEvent(
                eventId,
                paidOrder,
                PaymentStatus.PAID,
                now,
                now
        );

        paymentEventRepository.save(paymentEvent);
    }

    private record MolliePaymentResponse(
            String id,
            String status,
            MollieAmount amount,
            MollieMetadata metadata
    ) {
    }

    private record MollieAmount(
            String currency,
            String value
    ) {
    }

    private record MollieMetadata(
            Long orderId
    ) {
    }
}