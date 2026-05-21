package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.ConcertNotFoundException;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderItem;
import accordion_symphonic.ticketing.order.OrderNotFoundException;
import accordion_symphonic.ticketing.order.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    private final ConcertRepository conRepo;
    private final OrderRepository orderRepo;

    public TicketService(TicketRepository ticketRepository, ConcertRepository conRepo, OrderRepository orderRepo) {
        this.ticketRepository = ticketRepository;
        this.conRepo = conRepo;
        this.orderRepo = orderRepo;
    }

    public List<TicketResponse> createTicketsForOrder(Order order) {
        if (ticketRepository.existsByOrderId(order.getId())) {
            return ticketRepository.findByOrderId(order.getId())
                    .stream()
                    .map(TicketResponse::fromEntity)
                    .toList();
        }

        List<Ticket> tickets = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                tickets.add(new Ticket(order, item.getTicketCategory()));
            }
        }

        return ticketRepository.saveAll(tickets)
                .stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    public List<TicketResponse> getTicketsByConcertIdAndOrderId(Long concertId, Long orderId) {
        if (!conRepo.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = orderRepo.findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return ticketRepository.findByOrderId(order.getId())
                .stream()
                .map(TicketResponse::fromEntity)
                .toList();
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
        if (!conRepo.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        return ticketRepository.findByQrTokenAndOrderConcertId(qrToken, concertId)
                .orElseThrow(() -> new TicketNotFoundException(qrToken));
    }
}
