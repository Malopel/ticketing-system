package accordion_symphonic.ticketing.payment.service;

import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.service.OrderAccessTokenService;
import accordion_symphonic.ticketing.order.OrderProperties;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.OrderStatus;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.payment.PaymentProvider;
import accordion_symphonic.ticketing.payment.PaymentSession;
import accordion_symphonic.ticketing.payment.exception.OrderCannotBePaidException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderAccessTokenService orderAccessTokenService;
    private final PaymentProvider paymentProvider;
    private final OrderProperties orderProperties;

    public PaymentService(
            OrderRepository orderRepository,
            OrderAccessTokenService orderAccessTokenService,
            PaymentProvider paymentProvider,
            OrderProperties orderProperties
    ) {
        this.orderRepository = orderRepository;
        this.orderAccessTokenService = orderAccessTokenService;
        this.paymentProvider = paymentProvider;
        this.orderProperties = orderProperties;
    }

    @Transactional(
            noRollbackFor = OrderCannotBePaidException.class
    )
    public PaymentSession createPayment(
            Long concertId,
            Long orderId,
            String accessToken
    ) {
        Order order = orderRepository
                .findByIdAndConcertIdForUpdate(orderId, concertId)
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

            throw new OrderCannotBePaidException(orderId);
        }

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new OrderCannotBePaidException(orderId);
        }

        if (order.getConcert().getStatus()
                == ConcertStatus.CANCELLED) {
            throw new OrderCannotBePaidException(orderId);
        }

        order.markAsPaymentPending(
                LocalDateTime.now().plus(
                        orderProperties.paymentDuration()
                )
        );

        return paymentProvider.createPayment(order);
    }
}