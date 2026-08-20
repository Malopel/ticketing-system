package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderItem;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketCreationServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    private TicketCreationService ticketCreationService;

    @BeforeEach
    void setUp() {
        ticketCreationService =
                new TicketCreationService(ticketRepository);
    }

    @Test
    void createsTicketsForAllOrderItems() {
        Order order = createOrderWithItems();

        ReflectionTestUtils.setField(order, "id", 42L);

        when(ticketRepository.existsByOrderId(42L))
                .thenReturn(false);

        ticketCreationService.ensureTicketsCreatedForOrder(order);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Ticket>> ticketsCaptor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(ticketRepository)
                .saveAll(ticketsCaptor.capture());

        List<Ticket> savedTickets =
                StreamSupport.stream(
                        ticketsCaptor.getValue()
                                .spliterator(),
                        false
                ).toList();

        assertEquals(5, savedTickets.size());

        TicketCategory vip =
                order.getItems().get(0).getTicketCategory();

        TicketCategory normal =
                order.getItems().get(1).getTicketCategory();

        assertEquals(
                2,
                savedTickets.stream()
                        .filter(ticket ->
                                ticket.getTicketCategory() == vip
                        )
                        .count()
        );

        assertEquals(
                3,
                savedTickets.stream()
                        .filter(ticket ->
                                ticket.getTicketCategory() == normal
                        )
                        .count()
        );

        for (Ticket ticket : savedTickets) {
            assertSame(order, ticket.getOrder());
            assertEquals(TicketStatus.VALID, ticket.getStatus());
        }
    }

    @Test
    void doesNotCreateTicketsWhenTicketsAlreadyExist() {
        Order order = createOrderWithItems();

        ReflectionTestUtils.setField(order, "id", 42L);

        when(ticketRepository.existsByOrderId(42L))
                .thenReturn(true);

        ticketCreationService.ensureTicketsCreatedForOrder(order);

        verify(ticketRepository, never())
                .saveAll(any());

        verify(ticketRepository)
                .existsByOrderId(42L);
    }

    private Order createOrderWithItems() {
        Concert concert = new Concert(
                "Accordion Night",
                "Ein Testkonzert",
                LocalDateTime.now().plusDays(30),
                "Karlsruhe"
        );

        TicketCategory vip = new TicketCategory(
                "VIP",
                new BigDecimal("100.00"),
                50,
                concert
        );

        TicketCategory normal = new TicketCategory(
                "Normalpreis",
                new BigDecimal("25.00"),
                100,
                concert
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30)
        );

        order.addItem(
                new OrderItem(
                        vip,
                        2,
                        vip.getPrice()
                )
        );

        order.addItem(
                new OrderItem(
                        normal,
                        3,
                        normal.getPrice()
                )
        );

        return order;
    }
}