package accordion_symphonic.ticketing.order.service;

import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminOrderQueryService {
    private final OrderRepository orderRepository;
    private final ConcertRepository concertRepository;

    public AdminOrderQueryService(OrderRepository orderRepository, ConcertRepository concertRepository) {
        this.orderRepository = orderRepository;
        this.concertRepository = concertRepository;
    }

    @Transactional
    public List<OrderResponse> getOrdersForConcert(Long concertId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        List<Order> orders = orderRepository.findByConcertId(concertId);

        for (Order order : orders) {
            if (order.shouldExpire() || order.shouldExpirePayment()) order.expire();
        }

        return orders.stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }
}
