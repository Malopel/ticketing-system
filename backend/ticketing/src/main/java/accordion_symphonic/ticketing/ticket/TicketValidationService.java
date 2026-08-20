package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.exception.TicketIsNotValidException;
import accordion_symphonic.ticketing.ticket.exception.TicketNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TicketValidationService {
    private final ConcertRepository concertRepository;
    private final TicketRepository ticketRepository;

    public TicketValidationService(ConcertRepository concertRepository, TicketRepository ticketRepository) {
        this.concertRepository = concertRepository;
        this.ticketRepository = ticketRepository;
    }

    public TicketResponse validateTicket(Long concertId, String qrToken) {
        Ticket ticket = findTicketForConcert(concertId, qrToken);

        return TicketResponse.fromEntity(ticket);
    }

    public TicketResponse useTicket(Long concertId, String qrToken) {
        Ticket ticket  = findTicketForConcert(concertId, qrToken);

        if (ticket.getStatus() != TicketStatus.VALID) {
            throw new TicketIsNotValidException(qrToken);
        }

        ticket.useTicket();

        Ticket savedTicket = ticketRepository.save(ticket);

        return TicketResponse.fromEntity(savedTicket);
    }

    private Ticket findTicketForConcert(Long concertId, String qrToken) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        return ticketRepository.findByQrTokenAndOrderConcertId(qrToken, concertId)
                .orElseThrow(() -> new TicketNotFoundException(qrToken));
    }
}
