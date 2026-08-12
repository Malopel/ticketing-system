package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.concert.Concert;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
