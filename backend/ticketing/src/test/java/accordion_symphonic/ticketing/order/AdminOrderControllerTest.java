package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.order.exception.OrderExceptionHandler;
import accordion_symphonic.ticketing.order.exception.OrderHasNoTicketsException;
import accordion_symphonic.ticketing.order.service.AdminOrderQueryService;
import accordion_symphonic.ticketing.order.web.AdminOrderController;
import accordion_symphonic.ticketing.security.SecurityConfig;
import accordion_symphonic.ticketing.ticket.service.TicketDeliveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import accordion_symphonic.ticketing.common.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminOrderController.class)
@Import({
        SecurityConfig.class,
        OrderExceptionHandler.class
})
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password"
})
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminOrderQueryService adminOrderQueryService;

    @MockitoBean
    private TicketDeliveryService ticketDeliveryService;

    @Test
    void downloadTicketPdfReturnsPdfForAdmin() throws Exception {
        Long concertId = 1L;
        Long orderId = 42L;
        byte[] pdfBytes = "%PDF-test".getBytes(StandardCharsets.UTF_8);

        when(ticketDeliveryService.createTicketPdfForOrder(concertId, orderId))
                .thenReturn(pdfBytes);

        mockMvc.perform(get("/api/admin/concerts/{concertId}/orders/{orderId}/tickets/pdf", concertId, orderId)
                        .header("Authorization", basicAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString("tickets-order-42.pdf")
                ))
                .andExpect(content().bytes(pdfBytes));

        verify(ticketDeliveryService).createTicketPdfForOrder(concertId, orderId);
    }

    @Test
    void downloadTicketPdfRequiresAdminLogin() throws Exception {
        mockMvc.perform(get("/api/admin/concerts/{concertId}/orders/{orderId}/tickets/pdf", 1L, 42L))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminOrderQueryService);
    }

    private String basicAuthHeader() {
        String credentials = "admin:test-password";
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return "Basic " + encodedCredentials;
    }

    @Test
    void resendTicketEmailReturnsNoContentForAdmin() throws Exception {
        Long concertId = 1L;
        Long orderId = 42L;

        mockMvc.perform(post("/api/admin/concerts/{concertId}/orders/{orderId}/tickets/resend-email", concertId, orderId)
                        .header("Authorization", basicAuthHeader()))
                .andExpect(status().isNoContent());

        verify(ticketDeliveryService).resendTicketEmail(concertId, orderId);
    }

    @Test
    void resendTicketEmailRequiresAdminLogin() throws Exception {
        mockMvc.perform(post("/api/admin/concerts/{concertId}/orders/{orderId}/tickets/resend-email", 1L, 42L))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminOrderQueryService);
    }

    @Test
    void downloadTicketPdfReturnsConflictWhenOrderHasNoTickets() throws Exception {
        Long concertId = 1L;
        Long orderId = 42L;

        doThrow(new OrderHasNoTicketsException(orderId))
                .when(ticketDeliveryService)
                .createTicketPdfForOrder(concertId, orderId);

        mockMvc.perform(get("/api/admin/concerts/{concertId}/orders/{orderId}/tickets/pdf", concertId, orderId)
                        .header("Authorization", basicAuthHeader()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_HAS_NO_TICKETS));

        verify(ticketDeliveryService).createTicketPdfForOrder(concertId, orderId);
    }

    @Test
    void resendTicketEmailReturnsConflictWhenOrderHasNoTickets() throws Exception {
        Long concertId = 1L;
        Long orderId = 42L;

        doThrow(new OrderHasNoTicketsException(orderId))
                .when(ticketDeliveryService)
                .resendTicketEmail(concertId, orderId);

        mockMvc.perform(post("/api/admin/concerts/{concertId}/orders/{orderId}/tickets/resend-email", concertId, orderId)
                        .header("Authorization", basicAuthHeader()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_HAS_NO_TICKETS));

        verify(ticketDeliveryService).resendTicketEmail(concertId, orderId);
    }
}