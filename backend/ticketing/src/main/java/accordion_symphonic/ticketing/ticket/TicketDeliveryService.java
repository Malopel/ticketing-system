package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.mail.TicketEmailService;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.exception.OrderHasNoTicketsException;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketDeliveryService {

    private final ConcertRepository concertRepository;
    private final OrderRepository orderRepository;

    private final TicketService ticketService;
    private final TicketPdfService ticketPdfService;
    private final TicketEmailService ticketEmailService;

    public TicketDeliveryService(
            ConcertRepository concertRepository,
            OrderRepository orderRepository,
            TicketService ticketService,
            TicketPdfService ticketPdfService,
            TicketEmailService ticketEmailService
    ) {
        this.concertRepository = concertRepository;
        this.orderRepository = orderRepository;
        this.ticketService = ticketService;
        this.ticketPdfService = ticketPdfService;
        this.ticketEmailService = ticketEmailService;
    }

    @Transactional(readOnly = true)
    public byte[] createTicketPdfForOrder(
            Long concertId,
            Long orderId
    ) {
        Order oder = getOrder(concertId, orderId);

        List<TicketResponse> tickets =
                getTickets(concertId, orderId);

        return ticketPdfService.createTicketPdf(oder, tickets);
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public void resendTicketEmail(
            Long concertId,
            Long orderId
    ) {
        Order order = getOrder(concertId, orderId);

        List<TicketResponse> tickets =
                getTickets(concertId, orderId);

        ticketEmailService.sendEmail(order, tickets);
    }

    private Order getOrder(Long concertId, Long orderId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        return orderRepository.findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private List<TicketResponse> getTickets(Long concertId, Long orderId) {
        List<TicketResponse> tickets =
                ticketService.getTicketsByConcertIdAndOrderId(concertId, orderId);

        if (tickets.isEmpty()) {
            throw new OrderHasNoTicketsException(orderId);
        }

        return tickets;
    }
}
