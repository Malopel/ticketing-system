package accordion_symphonic.ticketing.order.service;

import accordion_symphonic.ticketing.availability.TicketAvailabilityService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderItem;
import accordion_symphonic.ticketing.order.OrderProperties;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.dto.CreatedOrderResponse;
import accordion_symphonic.ticketing.order.dto.OrderItemRequest;
import accordion_symphonic.ticketing.order.dto.OrderRequest;
import accordion_symphonic.ticketing.order.exception.DuplicateTicketCategoryException;
import accordion_symphonic.ticketing.order.exception.TooManyTicketsInOrderException;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import accordion_symphonic.ticketing.ticketcategory.exception.TicketCategoryNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final ConcertRepository concertRepository;
    private final TicketAvailabilityService ticketAvailabilityService;
    private final OrderAccessTokenService orderAccessTokenService;
    private final OrderProperties orderProperties;

    public OrderCreationService(
            OrderRepository orderRepository,
            TicketCategoryRepository ticketCategoryRepository,
            ConcertRepository concertRepository,
            TicketAvailabilityService ticketAvailabilityService,
            OrderAccessTokenService orderAccessTokenService,
            OrderProperties orderProperties
    ) {
        this.orderRepository = orderRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.concertRepository = concertRepository;
        this.ticketAvailabilityService = ticketAvailabilityService;
        this.orderAccessTokenService = orderAccessTokenService;
        this.orderProperties = orderProperties;
    }

    @Transactional
    public CreatedOrderResponse createOrder(
            long concertId,
            OrderRequest request
    ) {
        Concert concert = concertRepository
                .findByIdAndStatus(
                        concertId,
                        ConcertStatus.PUBLISHED
                )
                .orElseThrow(
                        () -> new ConcertNotFoundException(concertId)
                );

        validateMaxTicketsPerOrder(request);

        OrderAccessTokenService.GeneratedOrderAccessToken accessToken =
                orderAccessTokenService.generateToken();

        LocalDateTime createdAt = LocalDateTime.now();

        Order order = new Order(
                concert,
                request.customerEmail(),
                accessToken.tokenHash(),
                createdAt,
                createdAt.plus(
                        orderProperties.reservationDuration()
                )
        );

        Set<Long> requestedCategoryIds = new HashSet<>();

        List<OrderItemRequest> orderedItems =
                request.items().stream()
                        .sorted(
                                Comparator.comparing(
                                        OrderItemRequest::ticketCategoryId
                                )
                        )
                        .toList();

        for (OrderItemRequest itemRequest : orderedItems) {

            if (!requestedCategoryIds.add(
                    itemRequest.ticketCategoryId()
            )) {
                throw new DuplicateTicketCategoryException(
                        itemRequest.ticketCategoryId()
                );
            }

            TicketCategory category =
                    ticketCategoryRepository
                            .findByIdAndConcertIdForUpdate(
                                    itemRequest.ticketCategoryId(),
                                    concertId
                            )
                            .orElseThrow(
                                    () -> new TicketCategoryNotFoundException(
                                            itemRequest.ticketCategoryId()
                                    )
                            );

            ticketAvailabilityService.ensureTicketsAvailable(
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

        Order savedOrder =
                orderRepository.save(order);

        return CreatedOrderResponse.fromEntity(
                savedOrder,
                accessToken.token()
        );
    }

    private void validateMaxTicketsPerOrder(
            OrderRequest request
    ) {
        int requestedTickets =
                request.items().stream()
                        .mapToInt(OrderItemRequest::quantity)
                        .sum();

        if (
                requestedTickets >
                        orderProperties.maxTicketsPerOrder()
        ) {
            throw new TooManyTicketsInOrderException(
                    requestedTickets,
                    orderProperties.maxTicketsPerOrder()
            );
        }
    }
}