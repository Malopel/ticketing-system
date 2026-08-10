package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.common.GlobalExceptionHandler;
import accordion_symphonic.ticketing.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminOrderController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password"
})
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void downloadTicketPdfReturnsPdfForAdmin() throws Exception {
        Long concertId = 1L;
        Long orderId = 42L;
        byte[] pdfBytes = "%PDF-test".getBytes(StandardCharsets.UTF_8);

        when(orderService.createTicketPdfForOrder(concertId, orderId))
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

        verify(orderService).createTicketPdfForOrder(concertId, orderId);
    }

    @Test
    void downloadTicketPdfRequiresAdminLogin() throws Exception {
        mockMvc.perform(get("/api/admin/concerts/{concertId}/orders/{orderId}/tickets/pdf", 1L, 42L))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    private String basicAuthHeader() {
        String credentials = "admin:test-password";
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return "Basic " + encodedCredentials;
    }
}