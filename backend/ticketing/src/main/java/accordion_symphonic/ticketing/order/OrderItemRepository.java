package accordion_symphonic.ticketing.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        select coalesce(sum(item.quantity), 0)
        from OrderItem item
        where item.ticketCategory.id = :ticketCategoryId
        and (
            item.order.status = accordion_symphonic.ticketing.order.OrderStatus.PAID
            or (
                item.order.status = accordion_symphonic.ticketing.order.OrderStatus.RESERVED
                and item.order.expiresAt > :now
            )
        )
        """)
    int countBlockingTicketsByTicketCategoryId(Long ticketCategoryId, LocalDateTime now);
}