package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.concert.ConcertNotFoundException;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryNotFoundException;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final TicketCategoryRepository ticketCategoryRepository;
    private final ConcertRepository concertRepository;

    public OrderService(
            OrderRepository orderRepository,
            TicketCategoryRepository ticketCategoryRepository,
            ConcertRepository concertRepository
    ) {
        this.orderRepository = orderRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.concertRepository = concertRepository;
    }

    public OrderResponse getOrderByConcertIdAndId(Long concertId, Long orderId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        return this.orderRepository.findByIdAndConcertId(orderId, concertId)
                .map(OrderResponse::fromEntity)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public OrderResponse createOrder(long concertId, OrderRequest request) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = new Order(
                this.concertRepository.getReferenceById(concertId),
                request.customerEmail(),
                LocalDateTime.now()
        );

        for (OrderItemRequest itemRequest : request.items()) {
            TicketCategory category = ticketCategoryRepository
                    .findByIdAndConcertId(itemRequest.ticketCategoryId(), concertId)
                    .orElseThrow(() -> new TicketCategoryNotFoundException(itemRequest.ticketCategoryId()));

            OrderItem item = new OrderItem(
                    category,
                    itemRequest.quantity(),
                    category.getPrice()
            );

            order.addItem(item);
        }

        Order savedOrder = this.orderRepository.save(order);

        return OrderResponse.fromEntity(savedOrder);
    }

    public OrderResponse cancelOrder(Long concertId, Long orderId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = this.orderRepository
                .findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if(order.isPaidOrExpired()) {
            throw new OrderIsPaidOrExpiredException(orderId);
        }
        order.cancel();

        Order savedOrder = this.orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    public OrderResponse markOrderAsPaid(Long concertId, Long orderId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = this.orderRepository
                .findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if(order.isCancelledOrExpired()) {
            throw new OrderIsExpiredOrCancelledException(orderId);
        }

        order.markAsPaid();

        Order savedOrder = this.orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }
}
