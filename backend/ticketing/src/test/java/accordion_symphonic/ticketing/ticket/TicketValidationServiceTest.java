package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.exception.TicketIsNotValidException;
import accordion_symphonic.ticketing.ticket.exception.TicketNotFoundException;
import accordion_symphonic.ticketing.ticket.service.TicketValidationService;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketValidationServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private TicketRepository ticketRepository;

    private TicketValidationService ticketValidationService;

    @BeforeEach
    void setUp() {
        ticketValidationService = new TicketValidationService(
                concertRepository,
                ticketRepository
        );
    }


    @Test
    void validateTicketReturnsTicketWithoutUsingIt() {
        Long concertId = 1L;
        Ticket ticket = createValidTicket();

        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(ticketRepository.findByQrTokenAndOrderConcertId(ticket.getQrToken(), concertId))
                .thenReturn(Optional.of(ticket));

        TicketResponse response = ticketValidationService.validateTicket(concertId, ticket.getQrToken());

        assertEquals(ticket.getQrToken(), response.qrToken());
        assertEquals(TicketStatus.VALID, response.status());

        verify(ticketRepository, never()).save(ticket);
    }

    @Test
    void useTicketMarksValidTicketAsUsed() {
        Long concertId = 1L;
        Ticket ticket = createValidTicket();

        when(concertRepository.existsById(concertId))
                .thenReturn(true);

        when(ticketRepository.findByQrTokenAndOrderConcertIdForUpdate(
                ticket.getQrToken(),
                concertId
        )).thenReturn(Optional.of(ticket));

        TicketResponse response =
                ticketValidationService.useTicket(
                        concertId,
                        ticket.getQrToken()
                );

        assertEquals(TicketStatus.USED, response.status());
        assertEquals(TicketStatus.USED, ticket.getStatus());

        verify(ticketRepository)
                .findByQrTokenAndOrderConcertIdForUpdate(
                        ticket.getQrToken(),
                        concertId
                );

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void useTicketRejectsAlreadyUsedTicket() {
        Long concertId = 1L;

        Ticket ticket = createValidTicket();
        String qrToken = ticket.getQrToken();

        ticket.useTicket();

        assertEquals(
                TicketStatus.USED,
                ticket.getStatus()
        );

        when(concertRepository.existsById(concertId))
                .thenReturn(true);

        when(ticketRepository.findByQrTokenAndOrderConcertIdForUpdate(
                qrToken,
                concertId
        )).thenReturn(Optional.of(ticket));

        assertThrows(
                TicketIsNotValidException.class,
                () -> ticketValidationService.useTicket(
                        concertId,
                        qrToken
                )
        );

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void validateTicketThrowsWhenConcertDoesNotExist() {
        Long concertId = 1L;

        when(concertRepository.existsById(concertId))
                .thenReturn(false);

        assertThrows(
                ConcertNotFoundException.class,
                () -> ticketValidationService.validateTicket(
                        concertId,
                        "qr-token"
                )
        );

        verifyNoInteractions(ticketRepository);
    }

    @Test
    void validateTicketThrowsWhenTicketDoesNotExistForConcert() {
        Long concertId = 1L;
        String qrToken = "qr-token";

        when(concertRepository.existsById(concertId))
                .thenReturn(true);

        when(ticketRepository.findByQrTokenAndOrderConcertId(
                qrToken,
                concertId
        )).thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> ticketValidationService.validateTicket(
                        concertId,
                        qrToken
                )
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
                LocalDateTime.now().plusMinutes(30),
                LocalDateTime.now().plusDays(7)
        );

        return new Ticket(order, ticketCategory);
    }
}
