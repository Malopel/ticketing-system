package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketQueryService {

    private final TicketRepository ticketRepository;
    private final ConcertRepository concertRepository;
    private final OrderRepository orderRepository;

    public TicketQueryService(
            TicketRepository ticketRepository,
            ConcertRepository concertRepository,
            OrderRepository orderRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.concertRepository = concertRepository;
        this.orderRepository = orderRepository;
    }

    public List<TicketResponse> getTicketsByConcertIdAndOrderId(
            Long concertId,
            Long orderId
    ) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = orderRepository
                .findByIdAndConcertId(orderId, concertId)
                .orElseThrow(
                        () -> new OrderNotFoundException(orderId)
                );

        return ticketRepository.findByOrderId(order.getId())
                .stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }
}
