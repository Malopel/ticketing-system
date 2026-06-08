package accordion_symphonic.ticketing.availability;

import accordion_symphonic.ticketing.order.OrderItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TicketAvailabilityService {

    private final OrderItemRepository orderItemRepository;

    public TicketAvailabilityService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }
    public int getAvailableTickets(Long ticketCategoryId, int capacity) {
        int blockingTickets = orderItemRepository.countBlockingTicketsByTicketCategoryId(
                ticketCategoryId,
                LocalDateTime.now()
        );

        return capacity - blockingTickets;
    }

    public void ensureTicketsAvailable(Long ticketCategoryId, int capacity, int requestedQuantity) {
        int available = getAvailableTickets(ticketCategoryId, capacity);

        if (requestedQuantity > available) {
            throw new NotEnoughTicketsAvailableException(ticketCategoryId, available);
        }
    }
}