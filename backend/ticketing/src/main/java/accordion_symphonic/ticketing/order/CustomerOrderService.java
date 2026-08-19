package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.order.dto.OrderResponse;
import accordion_symphonic.ticketing.order.exception.*;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CustomerOrderService {

    private final OrderRepository orderRepository;

    private final ConcertRepository concertRepository;

    private final OrderAccessTokenService orderAccessTokenService;

    public CustomerOrderService(
            OrderRepository orderRepository,
            ConcertRepository concertRepository,
            OrderAccessTokenService orderAccessTokenService
    ) {
        this.orderRepository = orderRepository;
        this.concertRepository = concertRepository;
        this.orderAccessTokenService = orderAccessTokenService;
    }

    @Transactional
    public OrderResponse getCustomerOrder(Long concertId, Long orderId, String accessToken) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = orderRepository.findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        ensureCustomerHasAccess(order, accessToken);
        expireOrderIfNeeded(order);

        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse cancelOrder(
            Long concertId,
            Long orderId,
            String accessToken
    ) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = orderRepository
                .findByIdAndConcertIdForUpdate(
                        orderId,
                        concertId
                )
                .orElseThrow(
                        () -> new OrderNotFoundException(orderId)
                );

        ensureCustomerHasAccess(order, accessToken);

        expireOrderIfNeeded(order);

        if (
                order.getStatus() != OrderStatus.RESERVED &&
                        order.getStatus() != OrderStatus.CANCELLED
        ) {
            throw new OrderCannotBeCancelledException(orderId);
        }

        order.cancel();

        return OrderResponse.fromEntity(
                orderRepository.save(order)
        );
    }

    private void ensureCustomerHasAccess(Order order, String accessToken) {
        boolean hasAccess = orderAccessTokenService.matches(
                accessToken,
                order.getAccessTokenHash()
        );

        if ((!hasAccess)) {
            throw new OrderNotFoundException(order.getId());
        }
    }

    private void expireOrderIfNeeded(Order order) {
        if (
                order.shouldExpire() ||
                order.shouldExpirePayment()
        ) {
            order.expire();
        }
    }
}
