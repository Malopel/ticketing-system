package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderAccessTokenService;
import accordion_symphonic.ticketing.order.OrderNotFoundException;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.OrderStatus;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderAccessTokenService orderAccessTokenService;
    private final PaymentProvider paymentProvider;

    public PaymentService(
            OrderRepository orderRepository,
            OrderAccessTokenService orderAccessTokenService,
            PaymentProvider paymentProvider
    ) {
        this.orderRepository = orderRepository;
        this.orderAccessTokenService = orderAccessTokenService;
        this.paymentProvider = paymentProvider;
    }

    public PaymentSession createPayment(
            Long concertId,
            Long orderId,
            String accessToken
    ) {
        Order order = orderRepository
                .findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        boolean hasAccess = orderAccessTokenService.matches(
                accessToken,
                order.getAccessTokenHash()
        );

        if (!hasAccess) {
            throw new OrderNotFoundException(orderId);
        }

        if (order.shouldExpire()) {
            order.expire();
            orderRepository.save(order);

            throw new OrderCannotBePaidException(orderId);
        }

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new OrderCannotBePaidException(orderId);
        }

        return paymentProvider.createPayment(order);
    }
}