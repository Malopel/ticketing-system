package accordion_symphonic.ticketing.ticket.service;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderItem;
import accordion_symphonic.ticketing.ticket.Ticket;
import accordion_symphonic.ticketing.ticket.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketCreationService {

    private final TicketRepository ticketRepository;

    public TicketCreationService(
            TicketRepository ticketRepository
    ) {
        this.ticketRepository = ticketRepository;
    }

    public void ensureTicketsCreatedForOrder(Order order) {
        if (ticketRepository.existsByOrderId(order.getId())) {
            return;
        }

        List<Ticket> tickets = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                tickets.add(
                        new Ticket(
                                order,
                                item.getTicketCategory()
                        )
                );
            }
        }

        ticketRepository.saveAll(tickets);
    }
}