package accordion_symphonic.ticketing.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

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
                ) or (
                    item.order.status = accordion_symphonic.ticketing.order.OrderStatus.PAYMENT_PENDING
                    and item.order.paymentExpiresAt > :now
                )
            )
            """)
    int countBlockingTicketsByTicketCategoryId(
            @Param("ticketCategoryId") Long ticketCategoryId,
            @Param("now") LocalDateTime now
    );
}