package accordion_symphonic.ticketing.ticket.service;

import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.ticket.TicketRepository;
import accordion_symphonic.ticketing.ticket.dto.TicketOrderData;
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
        return getTicketOrderData(
                concertId,
                orderId
        ).tickets();
    }

    public TicketOrderData getTicketOrderData(
            Long concertId,
            Long orderId
    ) {
        Order order = orderRepository
                .findByIdAndConcertId(orderId, concertId)
                .orElseGet(() -> {
                    if (!concertRepository.existsById(concertId)) {
                        throw new ConcertNotFoundException(concertId);
                    }

                    throw new OrderNotFoundException(orderId);
                });

        List<TicketResponse> tickets =
                ticketRepository.findByOrderId(orderId)
                        .stream()
                        .map(TicketResponse::fromEntity)
                        .toList();

        return new TicketOrderData(order, tickets);
    }
}

