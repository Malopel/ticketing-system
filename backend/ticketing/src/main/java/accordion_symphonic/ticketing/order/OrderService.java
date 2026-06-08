package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.availability.TicketAvailabilityService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertNotFoundException;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.payment.OrderCannotBePaidException;
import accordion_symphonic.ticketing.ticket.TicketService;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryNotFoundException;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final TicketCategoryRepository ticketCategoryRepository;
    private final ConcertRepository concertRepository;

    private final TicketService ticketService;
    private final TicketAvailabilityService ticketAvailabilityService;

    private final OrderAccessTokenService orderAccessTokenService;

    public OrderService(
            OrderRepository orderRepository,
            TicketCategoryRepository ticketCategoryRepository,
            ConcertRepository concertRepository,
            TicketService ticketService,
            TicketAvailabilityService ticketAvailabilityService,
            OrderAccessTokenService orderAccessTokenService
    ) {
        this.orderRepository = orderRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.concertRepository = concertRepository;
        this.ticketService = ticketService;
        this.ticketAvailabilityService = ticketAvailabilityService;
        this.orderAccessTokenService = orderAccessTokenService;
    }

    public OrderResponse getCustomerOrder(Long concertId, Long orderId, String accessToken) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = this.orderRepository.findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        ensureCustomerHasAccess(order, accessToken);

        return OrderResponse.fromEntity(order);
    }

    public CreatedOrderResponse createOrder(long concertId, OrderRequest request) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new ConcertNotFoundException(concertId));

        OrderAccessTokenService.GeneratedOrderAccessToken accessToken =
                orderAccessTokenService.generateToken();

        Order order = new Order(
                concert,
                request.customerEmail(),
                accessToken.tokenHash(),
                LocalDateTime.now()
        );

        Set<Long> requestedCategoryIds = new HashSet<>();

        for (OrderItemRequest itemRequest : request.items()) {
            if (!requestedCategoryIds.add(itemRequest.ticketCategoryId())) {
                throw new DuplicateTicketCategoryException(itemRequest.ticketCategoryId());
            }

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

        return CreatedOrderResponse.fromEntity(savedOrder, accessToken.token());
    }

    public OrderResponse cancelOrder(Long concertId, Long orderId, String accessToken) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = this.orderRepository
                .findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        ensureCustomerHasAccess(order, accessToken);

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

        Order order = orderRepository.findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Order savedOrder = markOrderPaid(order);

        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional
    public List<OrderResponse> getOrdersForConcert(Long concertId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        List<Order> orders = orderRepository.findByConcertId(concertId);

        for (Order order : orders) {
            if (order.shouldExpire()) {
                order.expire();
            }
        }

        return orders.stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    public Order markOrderPaidFromPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return markOrderPaid(order);
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

    private Order markOrderPaid(Order order) {
        if (order.getStatus() == OrderStatus.PAID) {
            return order;
        }

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new OrderCannotBePaidException(order.getId());
        }

        order.markAsPaid();

        Order savedOrder = orderRepository.save(order);

        ticketService.createTicketsForOrder(savedOrder);

        return savedOrder;
    }
}
