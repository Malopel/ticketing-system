package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.availability.TicketAvailabilityService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertNotFoundException;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.ticket.TicketService;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryNotFoundException;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final TicketCategoryRepository ticketCategoryRepository;
    private final ConcertRepository concertRepository;

    private final TicketService ticketService;
    private final TicketAvailabilityService ticketAvailabilityService;

    public OrderService(
            OrderRepository orderRepository,
            TicketCategoryRepository ticketCategoryRepository,
            ConcertRepository concertRepository,
            TicketService ticketService, TicketAvailabilityService ticketAvailabilityService
    ) {
        this.orderRepository = orderRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.concertRepository = concertRepository;
        this.ticketService = ticketService;
        this.ticketAvailabilityService = ticketAvailabilityService;
    }

    public List<OrderResponse> getOrderForConcert(Long concertId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        return orderRepository.findByConcertId(concertId)
                .stream()
                .peek(this::expireIfNecessary)
                .map(OrderResponse::fromEntity)
                .toList();
    }

    public OrderResponse getOrderByConcertIdAndId(Long concertId, Long orderId) {
        Order order = getOrderForConcert(concertId, orderId);

        return OrderResponse.fromEntity(order);
    }

    public OrderResponse createOrder(long concertId, OrderRequest request) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new ConcertNotFoundException(concertId));

        Order order = new Order(
                concert,
                request.customerEmail(),
                LocalDateTime.now()
        );

        for (OrderItemRequest itemRequest : request.items()) {
            TicketCategory category = ticketCategoryRepository
                    .findByIdAndConcertId(itemRequest.ticketCategoryId(), concertId)
                    .orElseThrow(() -> new TicketCategoryNotFoundException(itemRequest.ticketCategoryId()));

            this.ticketAvailabilityService.ensureTicketsAvailable(
                    category.getId(),
                    category.getCapacity(),
                    itemRequest.quantity()
            );

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
        Order order = getOrderForConcert(concertId, orderId);

        if(order.isPaid() || order.isExpired()) {
            throw new OrderIsPaidOrExpiredException(orderId);
        }
        order.cancel();

        Order savedOrder = this.orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    public OrderResponse markOrderAsPaid(Long concertId, Long orderId) {
        Order order = getOrderForConcert(concertId, orderId);

        if (order.isCancelledOrExpired()) {
            throw new OrderIsExpiredOrCancelledException(orderId);
        }

        order.markAsPaid();

        Order savedOrder = this.orderRepository.save(order);

        this.ticketService.createTicketsForOrder(order);

        return OrderResponse.fromEntity(savedOrder);
    }

    private Order getOrderForConcert(Long concertId, Long orderId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = orderRepository.findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        expireIfNecessary(order);

        return order;
    }

    private void expireIfNecessary(Order order) {
        if (order.shouldExpire()) {
            order.expire();
            orderRepository.save(order);
        }
    }
}
