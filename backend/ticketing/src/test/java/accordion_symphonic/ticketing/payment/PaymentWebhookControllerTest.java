package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.common.GlobalExceptionHandler;
import accordion_symphonic.ticketing.payment.dto.PaymentWebhookRequest;
import accordion_symphonic.ticketing.payment.exception.InvalidPaymentWebhookSignatureException;
import accordion_symphonic.ticketing.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentWebhookController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password",
        "ticketing.payment.webhook-secret=test-webhook-secret"
})
class PaymentWebhookControllerTest {

    private static final String RAW_BODY =
            "{\"eventId\":\"evt_001\",\"orderId\":42,\"status\":\"PAID\"}";

    private static final String SIGNATURE = "valid-signature";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentWebhookService paymentWebhookService;

    @Test
    void validWebhookIsAcceptedWithoutAdminLogin() throws Exception {
        mockMvc.perform(post("/api/webhooks/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Webhook-Signature", SIGNATURE)
                        .content(RAW_BODY))
                .andExpect(status().isNoContent());

        verify(paymentWebhookService).processWebhook(
                eq(RAW_BODY),
                eq(SIGNATURE),
                any(PaymentWebhookRequest.class)
        );
    }

    @Test
    void invalidSignatureReturnsUnauthorized() throws Exception {
        doThrow(new InvalidPaymentWebhookSignatureException())
                .when(paymentWebhookService)
                .processWebhook(
                        eq(RAW_BODY),
                        eq("wrong-signature"),
                        any(PaymentWebhookRequest.class)
                );

        mockMvc.perform(post("/api/webhooks/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Webhook-Signature", "wrong-signature")
                        .content(RAW_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingSignatureReturnsUnauthorized() throws Exception {
        doThrow(new InvalidPaymentWebhookSignatureException())
                .when(paymentWebhookService)
                .processWebhook(
                        eq(RAW_BODY),
                        eq(null),
                        any(PaymentWebhookRequest.class)
                );

        mockMvc.perform(post("/api/webhooks/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RAW_BODY))
                .andExpect(status().isUnauthorized());
    }
}