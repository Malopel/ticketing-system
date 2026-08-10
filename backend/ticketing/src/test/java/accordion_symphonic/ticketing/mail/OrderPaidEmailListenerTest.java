package accordion_symphonic.ticketing.mail;

import accordion_symphonic.ticketing.order.OrderPaidEvent;
import accordion_symphonic.ticketing.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPaidEmailListenerTest {

    @Mock
    private OrderService orderService;

    private OrderPaidEmailListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderPaidEmailListener(orderService);
    }

    @Test
    void paidOrderTriggersTicketEmail() {
        OrderPaidEvent event =
                new OrderPaidEvent(1L, 42L);

        listener.handleOrderPaid(event);

        verify(orderService).resendTicketEmail(
                1L,
                42L
        );
    }

    @Test
    void emailFailureDoesNotEscapeListener() {
        OrderPaidEvent event =
                new OrderPaidEvent(1L, 42L);

        doThrow(new RuntimeException("SMTP unavailable"))
                .when(orderService)
                .resendTicketEmail(1L, 42L);

        assertDoesNotThrow(
                () -> listener.handleOrderPaid(event)
        );

        verify(orderService).resendTicketEmail(
                1L,
                42L
        );
    }
}