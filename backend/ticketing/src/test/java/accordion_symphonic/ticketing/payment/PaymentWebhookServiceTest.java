package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderService;
import accordion_symphonic.ticketing.payment.dto.PaymentWebhookRequest;
import accordion_symphonic.ticketing.payment.exception.InvalidPaymentWebhookSignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceTest {

    private static final String RAW_BODY =
            "{\"eventId\":\"evt_001\",\"orderId\":42,\"status\":\"PAID\"}";

    private static final String SIGNATURE = "valid-signature";

    @Mock
    private PaymentWebhookSignatureService signatureService;

    @Mock
    private PaymentEventRepository paymentEventRepository;

    @Mock
    private OrderService orderService;

    private PaymentWebhookService paymentWebhookService;

    private Order paidOrder;

    @BeforeEach
    void setUp() {
        paymentWebhookService = new PaymentWebhookService(
                signatureService,
                paymentEventRepository,
                orderService
        );

        Concert concert = new Concert(
                "Testkonzert",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Konzertsaal"
        );

        paidOrder = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );
    }

    @Test
    void validPaidWebhookMarksOrderAsPaidAndStoresPaymentEvent() {
        PaymentWebhookRequest request = new PaymentWebhookRequest(
                "evt_001",
                42L,
                PaymentStatus.PAID
        );

        when(signatureService.isValid(RAW_BODY, SIGNATURE))
                .thenReturn(true);

        when(paymentEventRepository.existsByEventId("evt_001"))
                .thenReturn(false);

        when(orderService.markOrderPaidFromPayment(42L))
                .thenReturn(paidOrder);

        paymentWebhookService.processWebhook(
                RAW_BODY,
                SIGNATURE,
                request
        );

        verify(orderService).markOrderPaidFromPayment(42L);

        ArgumentCaptor<PaymentEvent> paymentEventCaptor =
                ArgumentCaptor.forClass(PaymentEvent.class);

        verify(paymentEventRepository).save(paymentEventCaptor.capture());

        PaymentEvent savedEvent = paymentEventCaptor.getValue();

        assertEquals("evt_001", savedEvent.getEventId());
        assertEquals(paidOrder, savedEvent.getOrder());
        assertEquals(PaymentStatus.PAID, savedEvent.getPaymentStatus());
    }

    @Test
    void invalidSignatureIsRejectedBeforeChangingOrder() {
        PaymentWebhookRequest request = new PaymentWebhookRequest(
                "evt_001",
                42L,
                PaymentStatus.PAID
        );

        when(signatureService.isValid(RAW_BODY, SIGNATURE))
                .thenReturn(false);

        assertThrows(
                InvalidPaymentWebhookSignatureException.class,
                () -> paymentWebhookService.processWebhook(
                        RAW_BODY,
                        SIGNATURE,
                        request
                )
        );

        verifyNoInteractions(orderService);
        verify(paymentEventRepository, never()).save(any());
    }

    @Test
    void duplicateEventIsIgnored() {
        PaymentWebhookRequest request = new PaymentWebhookRequest(
                "evt_001",
                42L,
                PaymentStatus.PAID
        );

        when(signatureService.isValid(RAW_BODY, SIGNATURE))
                .thenReturn(true);

        when(paymentEventRepository.existsByEventId("evt_001"))
                .thenReturn(true);

        paymentWebhookService.processWebhook(
                RAW_BODY,
                SIGNATURE,
                request
        );

        verify(paymentEventRepository).existsByEventId("evt_001");
        verifyNoInteractions(orderService);
        verify(paymentEventRepository, never()).save(any());
    }

    @Test
    void duplicateWebhookOnlyMarksOrderPaidOnce() {
        PaymentWebhookRequest request = new PaymentWebhookRequest(
                "evt_001",
                42L,
                PaymentStatus.PAID
        );

        when(signatureService.isValid(RAW_BODY, SIGNATURE))
                .thenReturn(true);

        when(paymentEventRepository.existsByEventId("evt_001"))
                .thenReturn(false)
                .thenReturn(true);

        when(orderService.markOrderPaidFromPayment(42L))
                .thenReturn(paidOrder);

        paymentWebhookService.processWebhook(
                RAW_BODY,
                SIGNATURE,
                request
        );

        paymentWebhookService.processWebhook(
                RAW_BODY,
                SIGNATURE,
                request
        );

        verify(orderService, times(1)).markOrderPaidFromPayment(42L);
        verify(paymentEventRepository, times(1)).save(any(PaymentEvent.class));
    }
}