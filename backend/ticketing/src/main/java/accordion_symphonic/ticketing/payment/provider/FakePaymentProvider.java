package accordion_symphonic.ticketing.payment.provider;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.payment.PaymentProvider;
import accordion_symphonic.ticketing.payment.PaymentSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(
        name = "ticketing.payment.provider",
        havingValue = "fake"
)
public class FakePaymentProvider implements PaymentProvider {

    private final Map<String, Long> paymentOrders =
            new ConcurrentHashMap<>();

    @Override
    public PaymentSession createPayment(Order order) {
        String providerPaymentId = UUID.randomUUID().toString();

        paymentOrders.put(
                providerPaymentId,
                order.getId()
        );

        String checkoutUrl =
                "http://localhost:5173/fake-payment/"
                        + providerPaymentId;

        return new PaymentSession(
                providerPaymentId,
                checkoutUrl
        );
    }

    public Optional<Long> findOrderId(String providerPaymentId) {
        return Optional.ofNullable(
                paymentOrders.get(providerPaymentId)
        );
    }
}