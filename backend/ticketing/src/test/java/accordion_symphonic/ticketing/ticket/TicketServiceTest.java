package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertNotFoundException;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketServiceTest {

    private TicketRepository ticketRepository;
    private ConcertRepository concertRepository;
    private OrderRepository orderRepository;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        concertRepository = mock(ConcertRepository.class);
        orderRepository = mock(OrderRepository.class);

        ticketService = new TicketService(
                ticketRepository,
                concertRepository,
                orderRepository
        );
    }

    @Test
    void validateTicketReturnsTicketWithoutUsingIt() {
        Long concertId = 1L;
        Ticket ticket = createValidTicket();

        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(ticketRepository.findByQrTokenAndOrderConcertId(ticket.getQrToken(), concertId))
                .thenReturn(Optional.of(ticket));

        TicketResponse response = ticketService.validateTicket(concertId, ticket.getQrToken());

        assertEquals(ticket.getQrToken(), response.qrToken());
        assertEquals(TicketStatus.VALID, response.status());

        verify(ticketRepository, never()).save(ticket);
    }

    @Test
    void useTicketMarksValidTicketAsUsed() {
        Long concertId = 1L;
        Ticket ticket = createValidTicket();

        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(ticketRepository.findByQrTokenAndOrderConcertId(ticket.getQrToken(), concertId))
                .thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketResponse response = ticketService.useTicket(concertId, ticket.getQrToken());

        assertEquals(TicketStatus.USED, response.status());
        assertEquals(TicketStatus.USED, ticket.getStatus());

        verify(ticketRepository).save(ticket);
    }

    @Test
    void useTicketRejectsAlreadyUsedTicket() {
        Long concertId = 1L;
        Ticket ticket = createValidTicket();
        ticket.useTicket();

        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(ticketRepository.findByQrTokenAndOrderConcertId(ticket.getQrToken(), concertId))
                .thenReturn(Optional.of(ticket));

        assertThrows(
                TicketIsNotValidException.class,
                () -> ticketService.useTicket(concertId, ticket.getQrToken())
        );

        verify(ticketRepository, never()).save(ticket);
    }

    @Test
    void validateTicketThrowsWhenConcertDoesNotExist() {
        Long concertId = 1L;
        String qrToken = "unknown-token";

        when(concertRepository.existsById(concertId)).thenReturn(false);

        assertThrows(
                ConcertNotFoundException.class,
                () -> ticketService.validateTicket(concertId, qrToken)
        );

        verify(ticketRepository, never()).findByQrTokenAndOrderConcertId(qrToken, concertId);
    }

    @Test
    void validateTicketThrowsWhenTicketDoesNotExistForConcert() {
        Long concertId = 1L;
        String qrToken = "unknown-token";

        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(ticketRepository.findByQrTokenAndOrderConcertId(qrToken, concertId))
                .thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> ticketService.validateTicket(concertId, qrToken)
        );
    }

    private Ticket createValidTicket() {
        Concert concert = new Concert(
                "Accordion Night",
                "Ein Testkonzert",
                LocalDateTime.now().plusDays(30),
                "Karlsruhe"
        );

        TicketCategory ticketCategory = new TicketCategory(
                "VIP",
                BigDecimal.valueOf(100),
                50,
                concert
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now().plusMinutes(30)
        );

        return new Ticket(order, ticketCategory);
    }
}