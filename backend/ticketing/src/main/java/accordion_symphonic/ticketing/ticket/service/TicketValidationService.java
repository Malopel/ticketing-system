package accordion_symphonic.ticketing.ticket.service;

import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.concert.exception.ConcertIsCancelledException;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.ticket.Ticket;
import accordion_symphonic.ticketing.ticket.TicketRepository;
import accordion_symphonic.ticketing.ticket.TicketStatus;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.exception.TicketIsNotValidException;
import accordion_symphonic.ticketing.ticket.exception.TicketNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        ensureConcertIsNotCancelled(ticket);

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional
    public TicketResponse useTicket(Long concertId, String qrToken) {
        Ticket ticket  = findTicketForConcertForUpdate(concertId, qrToken);

        ensureConcertIsNotCancelled(ticket);

        ticket.useTicket();

        return TicketResponse.fromEntity(ticket);
    }

    private Ticket findTicketForConcert(Long concertId, String qrToken) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        return ticketRepository.findByQrTokenAndOrderConcertId(qrToken, concertId)
                .orElseThrow(() -> new TicketNotFoundException(qrToken));
    }

    private Ticket findTicketForConcertForUpdate(
            Long concertId,
            String qrToken
    ) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        return ticketRepository
                .findByQrTokenAndOrderConcertIdForUpdate(
                        qrToken,
                        concertId
                )
                .orElseThrow(
                        () -> new TicketNotFoundException(qrToken)
                );
    }

    private void ensureConcertIsNotCancelled(Ticket ticket) {
        if (ticket.getOrder().getConcert().getStatus()
                == ConcertStatus.CANCELLED) {
            throw new ConcertIsCancelledException(
                    ticket.getOrder().getConcert().getId()
            );
        }
    }
}
