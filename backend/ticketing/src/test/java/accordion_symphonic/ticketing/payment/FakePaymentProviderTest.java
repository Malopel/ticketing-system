package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.order.Order;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FakePaymentProviderTest {

    private final FakePaymentProvider paymentProvider =
            new FakePaymentProvider();

    @Test
    void createPaymentCreatesPaymentSessionAndRemembersOrder() {
        Order order = mock(Order.class);

        when(order.getId()).thenReturn(42L);

        PaymentSession session =
                paymentProvider.createPayment(order);

        assertNotNull(session.providerPaymentId());

        assertFalse(
                session.providerPaymentId().isBlank()
        );

        assertEquals(
                "http://localhost:5173/fake-payment/"
                        + session.providerPaymentId(),
                session.checkoutUrl()
        );

        assertEquals(
                Optional.of(42L),
                paymentProvider.findOrderId(
                        session.providerPaymentId()
                )
        );
    }

    @Test
    void findOrderIdReturnsEmptyForUnknownPayment() {
        assertTrue(
                paymentProvider
                        .findOrderId("does-not-exist")
                        .isEmpty()
        );
    }
}