package accordion_symphonic.ticketing.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            select coalesce(sum(item.quantity), 0)
            from OrderItem item
            where item.ticketCategory.id = :ticketCategoryId
            and item.order.status in :statuses
            """)
    int countByTicketCategoryIdAndOrderStatusIn(
            Long ticketCategoryId,
            List<OrderStatus> statuses
    );
}