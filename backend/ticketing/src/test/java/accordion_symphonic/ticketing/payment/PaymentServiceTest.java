package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.order.*;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.order.service.OrderAccessTokenService;
import accordion_symphonic.ticketing.payment.exception.OrderCannotBePaidException;
import accordion_symphonic.ticketing.payment.service.PaymentService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
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

    @Mock
    private Concert concert;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                orderRepository,
                orderAccessTokenService,
                paymentProvider,
                new OrderProperties(Duration.ofMinutes(30), 10, Duration.ofMinutes(20))
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

        when(orderRepository.findByIdAndConcertIdForUpdate(
                orderId,
                concertId
        )).thenReturn(Optional.of(order));

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

        when(order.getConcert())
                .thenReturn(concert);

        when(concert.getStatus())
                .thenReturn(ConcertStatus.PUBLISHED);

        when(paymentProvider.createPayment(order))
                .thenReturn(expectedSession);

        PaymentSession result = paymentService.createPayment(
                concertId,
                orderId,
                accessToken
        );

        assertEquals(expectedSession, result);

        verify(order).markAsPaymentPending(
                any(LocalDateTime.class)
        );

        verify(paymentProvider).createPayment(order);
    }

    @Test
    void createPaymentRejectsWrongAccessToken() {
        Long concertId = 1L;
        Long orderId = 2L;

        when(orderRepository.findByIdAndConcertIdForUpdate(
                orderId,
                concertId
        )).thenReturn(Optional.of(order));

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

        verify(order, never()).markAsPaymentPending(
                any()
        );
    }

    @Test
    void createPaymentRejectsPaidOrder() {
        Long concertId = 1L;
        Long orderId = 2L;

        String accessToken = "access-token";
        String accessTokenHash = "access-token-hash";

        when(orderRepository.findByIdAndConcertIdForUpdate(
                orderId,
                concertId
        )).thenReturn(Optional.of(order));

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

        verify(order, never()).markAsPaymentPending(
                any()
        );
    }

    @Test
    void createPaymentExpiresExpiredOrder() {
        Long concertId = 1L;
        Long orderId = 2L;

        String accessToken = "access-token";
        String accessTokenHash = "access-token-hash";

        when(orderRepository.findByIdAndConcertIdForUpdate(
                orderId,
                concertId
        )).thenReturn(Optional.of(order));

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

        verify(order, never()).markAsPaymentPending(
                any()
        );

        verifyNoInteractions(paymentProvider);
    }

    @Test
    void createPaymentRejectsCancelledConcert() {
        Long concertId = 1L;
        Long orderId = 2L;

        String accessToken = "access-token";
        String accessTokenHash = "access-token-hash";

        when(orderRepository.findByIdAndConcertIdForUpdate(
                orderId,
                concertId
        )).thenReturn(Optional.of(order));

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

        when(order.getConcert())
                .thenReturn(concert);

        when(concert.getStatus())
                .thenReturn(ConcertStatus.CANCELLED);

        assertThrows(
                OrderCannotBePaidException.class,
                () -> paymentService.createPayment(
                        concertId,
                        orderId,
                        accessToken
                )
        );

        verify(order, never())
                .markAsPaymentPending(any());

        verifyNoInteractions(paymentProvider);
    }
}