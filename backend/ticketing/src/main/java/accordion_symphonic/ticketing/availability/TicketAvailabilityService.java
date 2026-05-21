package accordion_symphonic.ticketing.availability;

import accordion_symphonic.ticketing.order.OrderItemRepository;
import accordion_symphonic.ticketing.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketAvailabilityService {

    private final OrderItemRepository orderItemRepository;

    public TicketAvailabilityService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public int getAvailableTickets(Long ticketCategoryId, int capacity) {
        int reservedOrPaid = orderItemRepository.countByTicketCategoryIdAndOrderStatusIn(
                ticketCategoryId,
                List.of(OrderStatus.RESERVED, OrderStatus.PAID)
        );

        return capacity - reservedOrPaid;
    }

    public void ensureTicketsAvailable(Long ticketCategoryId, int capacity, int requestedQuantity) {
        int available = getAvailableTickets(ticketCategoryId, capacity);

        if (requestedQuantity > available) {
            throw new NotEnoughTicketsAvailableException(ticketCategoryId, available);
        }
    }
}