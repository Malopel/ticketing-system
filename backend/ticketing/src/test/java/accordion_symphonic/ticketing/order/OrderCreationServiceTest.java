package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.availability.TicketAvailabilityService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.order.dto.OrderItemRequest;
import accordion_symphonic.ticketing.order.dto.OrderRequest;
import accordion_symphonic.ticketing.order.exception.DuplicateTicketCategoryException;
import accordion_symphonic.ticketing.order.exception.TooManyTicketsInOrderException;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderCreationServiceTest {
    @Mock
    private ConcertRepository concertRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    @Mock
    private OrderAccessTokenService orderAccessTokenService;

    @Mock
    private TicketAvailabilityService ticketAvailabilityService;


    private OrderCreationService orderCreationService;

    @BeforeEach
    void setUp() {
        orderCreationService = new OrderCreationService(
                orderRepository,
                ticketCategoryRepository,
                concertRepository,
                ticketAvailabilityService,
                orderAccessTokenService,
                new OrderProperties(Duration.ofMinutes(30), 10, Duration.ofMinutes(20))
        );
    }

    @Test
    void createOrderRejectsDuplicateTicketCategory() {
        Long concertId = 1L;
        Long ticketCategoryId = 7L;

        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        TicketCategory category = new TicketCategory(
                "Normalpreis",
                new BigDecimal("25.00"),
                100,
                concert
        );

        when(concertRepository.findByIdAndStatus(
                concertId,
                ConcertStatus.PUBLISHED
        ))
                .thenReturn(Optional.of(concert));

        when(orderAccessTokenService.generateToken())
                .thenReturn(new OrderAccessTokenService.GeneratedOrderAccessToken(
                        "plain-token",
                        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
                ));

        when(ticketCategoryRepository.findByIdAndConcertIdForUpdate(ticketCategoryId, concertId))
                .thenReturn(Optional.of(category));

        OrderRequest request = new OrderRequest(
                "kunde@example.com",
                List.of(
                        new OrderItemRequest(ticketCategoryId, 2),
                        new OrderItemRequest(ticketCategoryId, 3)
                )
        );

        assertThrows(
                DuplicateTicketCategoryException.class,
                () -> orderCreationService.createOrder(concertId, request)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderRejectsMoreThanMaxTicketsPerOrder() {
        long concertId = 1L;

        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        when(concertRepository.findByIdAndStatus(
                concertId,
                ConcertStatus.PUBLISHED
        ))
                .thenReturn(Optional.of(concert));

        OrderRequest request = new OrderRequest(
                "kunde@example.com",
                List.of(
                        new OrderItemRequest(7L, 6),
                        new OrderItemRequest(8L, 5)
                )
        );

        assertThrows(
                TooManyTicketsInOrderException.class,
                () -> orderCreationService.createOrder(concertId, request)
        );

        verify(orderAccessTokenService, never()).generateToken();
        verify(ticketCategoryRepository, never()).findByIdAndConcertIdForUpdate(any(), any());
        verify(orderRepository, never()).save(any());
    }

}
