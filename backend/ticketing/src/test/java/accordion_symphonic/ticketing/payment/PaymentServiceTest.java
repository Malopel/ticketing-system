package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.order.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderAccessTokenService orderAccessTokenService;

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private Order order;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                orderRepository,
                orderAccessTokenService,
                paymentProvider
        );
    }

    @Test
    void createPaymentCreatesPaymentSessionForReservedOrder() {
        Long concertId = 1L;
        Long orderId = 2L;

        String accessToken = "access-token";
        String accessTokenHash = "access-token-hash";

        PaymentSession expectedSession = new PaymentSession(
                "provider-payment-123",
                "http://localhost:5173/fake-payment/provider-payment-123"
        );

        when(orderRepository.findByIdAndConcertId(orderId, concertId))
                .thenReturn(Optional.of(order));

        when(order.getAccessTokenHash())
                .thenReturn(accessTokenHash);

        when(orderAccessTokenService.matches(
                accessToken,
                accessTokenHash
        )).thenReturn(true);

        when(order.shouldExpire())
                .thenReturn(false);

        when(order.getStatus())
                .thenReturn(OrderStatus.RESERVED);

        when(paymentProvider.createPayment(order))
                .thenReturn(expectedSession);

        PaymentSession result = paymentService.createPayment(
                concertId,
                orderId,
                accessToken
        );

        assertEquals(expectedSession, result);

        verify(paymentProvider).createPayment(order);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createPaymentRejectsWrongAccessToken() {
        Long concertId = 1L;
        Long orderId = 2L;

        when(orderRepository.findByIdAndConcertId(orderId, concertId))
                .thenReturn(Optional.of(order));

        when(order.getAccessTokenHash())
                .thenReturn("access-token-hash");

        when(orderAccessTokenService.matches(
                "wrong-token",
                "access-token-hash"
        )).thenReturn(false);

        assertThrows(
                OrderNotFoundException.class,
                () -> paymentService.createPayment(
                        concertId,
                        orderId,
                        "wrong-token"
                )
        );

        verifyNoInteractions(paymentProvider);
    }

    @Test
    void createPaymentRejectsPaidOrder() {
        Long concertId = 1L;
        Long orderId = 2L;

        String accessToken = "access-token";
        String accessTokenHash = "access-token-hash";

        when(orderRepository.findByIdAndConcertId(orderId, concertId))
                .thenReturn(Optional.of(order));

        when(order.getAccessTokenHash())
                .thenReturn(accessTokenHash);

        when(orderAccessTokenService.matches(
                accessToken,
                accessTokenHash
        )).thenReturn(true);

        when(order.shouldExpire())
                .thenReturn(false);

        when(order.getStatus())
                .thenReturn(OrderStatus.PAID);

        assertThrows(
                OrderCannotBePaidException.class,
                () -> paymentService.createPayment(
                        concertId,
                        orderId,
                        accessToken
                )
        );

        verifyNoInteractions(paymentProvider);
    }

    @Test
    void createPaymentExpiresExpiredOrder() {
        Long concertId = 1L;
        Long orderId = 2L;

        String accessToken = "access-token";
        String accessTokenHash = "access-token-hash";

        when(orderRepository.findByIdAndConcertId(orderId, concertId))
                .thenReturn(Optional.of(order));

        when(order.getAccessTokenHash())
                .thenReturn(accessTokenHash);

        when(orderAccessTokenService.matches(
                accessToken,
                accessTokenHash
        )).thenReturn(true);

        when(order.shouldExpire())
                .thenReturn(true);

        assertThrows(
                OrderCannotBePaidException.class,
                () -> paymentService.createPayment(
                        concertId,
                        orderId,
                        accessToken
                )
        );

        verify(order).expire();
        verify(orderRepository).save(order);
        verifyNoInteractions(paymentProvider);
    }
}