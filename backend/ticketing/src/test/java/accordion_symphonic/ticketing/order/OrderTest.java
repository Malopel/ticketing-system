package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.concert.Concert;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderTest {
    @Test
    void reservedOrderCanBeCancelled() {
        Order order = createReservedOrder();

        order.cancel();

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus()
        );
    }

    @Test
    void paidOrderCannotBeCancelled() {
        Order order = createReservedOrder();

        order.markAsPaymentPending(
                LocalDateTime.now().plusMinutes(20)
        );

        order.markAsPaid();

        assertThrows(
                IllegalStateException.class,
                order::cancel
        );
    }

    @Test
    void cancelledOrderCannotBePaid() {
        Order order = createReservedOrder();

        order.cancel();

        assertThrows(
                IllegalStateException.class,
                order::markAsPaid
        );
    }

    @Test
    void cancellingCancelledOrderIsIdempotent() {
        Order order = createReservedOrder();

        order.cancel();
        order.cancel();

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus()
        );
    }

    @Test
    void paymentPendingOrderCanBeMarkedAsPaid() {
        Order order = createReservedOrder();

        order.markAsPaymentPending(
                LocalDateTime.now().plusMinutes(20)
        );

        order.markAsPaid();

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertNotNull(order.getPaidAt());
    }

    @Test
    void reservedOrderCannotBeMarkedAsPaid() {
        Order order = createReservedOrder();

        assertThrows(
                IllegalStateException.class,
                order::markAsPaid
        );

        assertEquals(OrderStatus.RESERVED, order.getStatus());
    }

    @Test
    void paymentPendingOrderCanExpire() {
        Order order = createReservedOrder();

        order.markAsPaymentPending(
                LocalDateTime.now().plusMinutes(20)
        );

        order.expire();

        assertEquals(OrderStatus.EXPIRED, order.getStatus());
    }

    @Test
    void paymentPendingOrderCannotBeCancelled() {
        Order order = createReservedOrder();

        order.markAsPaymentPending(
                LocalDateTime.now().plusMinutes(20)
        );

        assertThrows(
                IllegalStateException.class,
                order::cancel
        );

        assertEquals(
                OrderStatus.PAYMENT_PENDING,
                order.getStatus()
        );
    }

    @Test
    void paidOrderCannotBecomePaymentPending() {
        Order order = createReservedOrder();

        order.markAsPaymentPending(
                LocalDateTime.now().plusMinutes(20)
        );
        order.markAsPaid();

        assertThrows(
                IllegalStateException.class,
                () -> order.markAsPaymentPending(
                        LocalDateTime.now().plusMinutes(20)
                )
        );

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    private Order createReservedOrder() {
        Concert concert = new Concert(
                "Testkonzert",
                "Beschreibung",
                LocalDateTime.now().plusDays(10),
                "Heidelberg"
        );

        return new Order(
                concert,
                "test@example.de",
                "token-hash",
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30)
        );
    }
}
