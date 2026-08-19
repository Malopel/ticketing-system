package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.availability.TicketAvailabilityService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.order.dto.CreatedOrderResponse;
import accordion_symphonic.ticketing.order.dto.OrderItemRequest;
import accordion_symphonic.ticketing.order.dto.OrderRequest;
import accordion_symphonic.ticketing.order.dto.OrderResponse;
import accordion_symphonic.ticketing.order.exception.*;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.exception.TicketCategoryNotFoundException;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final TicketCategoryRepository ticketCategoryRepository;
    private final ConcertRepository concertRepository;

    private final TicketAvailabilityService ticketAvailabilityService;

    private final OrderAccessTokenService orderAccessTokenService;

    private final OrderProperties orderProperties;

    public OrderService(
            OrderRepository orderRepository,
            TicketCategoryRepository ticketCategoryRepository,
            ConcertRepository concertRepository,
            TicketAvailabilityService ticketAvailabilityService,
            OrderAccessTokenService orderAccessTokenService,
            OrderProperties orderProperties) {
        this.orderRepository = orderRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.concertRepository = concertRepository;
        this.ticketAvailabilityService = ticketAvailabilityService;
        this.orderAccessTokenService = orderAccessTokenService;
        this.orderProperties = orderProperties;
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
    public CreatedOrderResponse createOrder(long concertId, OrderRequest request) {
        Concert concert = concertRepository
                .findByIdAndStatus(concertId, ConcertStatus.PUBLISHED)
                .orElseThrow(() -> new ConcertNotFoundException(concertId));

        validateMaxTicketsPerOrder(request);

        OrderAccessTokenService.GeneratedOrderAccessToken accessToken =
                orderAccessTokenService.generateToken();

        LocalDateTime createdAt = LocalDateTime.now();

        Order order = new Order(
                concert,
                request.customerEmail(),
                accessToken.tokenHash(),
                createdAt,
                createdAt.plus(orderProperties.reservationDuration())
        );

        Set<Long> requestedCategoryIds = new HashSet<>();

        List<OrderItemRequest> orderedItems = request.items().stream()
                .sorted(Comparator.comparing(OrderItemRequest::ticketCategoryId))
                .toList();

        for (OrderItemRequest itemRequest : orderedItems) {
            if (!requestedCategoryIds.add(itemRequest.ticketCategoryId())) {
                throw new DuplicateTicketCategoryException(itemRequest.ticketCategoryId());
            }

            TicketCategory category = ticketCategoryRepository
                    .findByIdAndConcertIdForUpdate(itemRequest.ticketCategoryId(), concertId)
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

    @Transactional
    public List<OrderResponse> getOrdersForConcert(Long concertId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        List<Order> orders = orderRepository.findByConcertId(concertId);

        for (Order order : orders) {
            expireOrderIfNeeded(order);
        }

        return orders.stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    private void validateMaxTicketsPerOrder(OrderRequest request) {
        int requestedTickets = request.items().stream()
                .mapToInt(OrderItemRequest::quantity)
                .sum();

        if (requestedTickets > orderProperties.maxTicketsPerOrder()) {
            throw new TooManyTicketsInOrderException(
                    requestedTickets,
                    orderProperties.maxTicketsPerOrder()
            );
        }
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
