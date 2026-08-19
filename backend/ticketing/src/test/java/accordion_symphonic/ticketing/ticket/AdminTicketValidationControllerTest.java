package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.security.SecurityConfig;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.exception.TicketExceptionHandler;
import accordion_symphonic.ticketing.ticket.exception.TicketIsNotValidException;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminTicketValidationController.class)
@Import({
        SecurityConfig.class,
        TicketExceptionHandler.class
})
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password"
})
class AdminTicketValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void validateTicketReturnsTicketForAdmin() throws Exception {
        Long concertId = 1L;
        Ticket ticket = createValidTicket();
        String qrToken = ticket.getQrToken();

        when(ticketService.validateTicket(concertId, qrToken))
                .thenReturn(TicketResponse.fromEntity(ticket));

        mockMvc.perform(get("/api/admin/concerts/{concertId}/tickets/validate/{qrToken}", concertId, qrToken)
                        .header("Authorization", basicAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrToken").value(qrToken))
                .andExpect(jsonPath("$.status").value("VALID"));

        verify(ticketService).validateTicket(concertId, qrToken);
    }

    @Test
    void useTicketMarksTicketAsUsedForAdmin() throws Exception {
        Long concertId = 1L;
        Ticket ticket = createValidTicket();
        String qrToken = ticket.getQrToken();

        ticket.useTicket();

        when(ticketService.useTicket(concertId, qrToken))
                .thenReturn(TicketResponse.fromEntity(ticket));

        mockMvc.perform(patch("/api/admin/concerts/{concertId}/tickets/{qrToken}/use", concertId, qrToken)
                        .header("Authorization", basicAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrToken").value(qrToken))
                .andExpect(jsonPath("$.status").value("USED"));

        verify(ticketService).useTicket(concertId, qrToken);
    }

    @Test
    void useTicketReturnsConflictWhenTicketIsAlreadyUsed() throws Exception {
        Long concertId = 1L;
        String qrToken = "already-used-ticket";

        doThrow(new TicketIsNotValidException(qrToken))
                .when(ticketService)
                .useTicket(concertId, qrToken);

        mockMvc.perform(patch("/api/admin/concerts/{concertId}/tickets/{qrToken}/use", concertId, qrToken)
                        .header("Authorization", basicAuthHeader()))
                .andExpect(status().isConflict());
    }

    @Test
    void validateTicketRequiresAdminLogin() throws Exception {
        mockMvc.perform(get("/api/admin/concerts/{concertId}/tickets/validate/{qrToken}", 1L, "some-token"))
                .andExpect(status().isUnauthorized());
    }

    private Ticket createValidTicket() {
        Concert concert = new Concert(
                "Accordion Night",
                "Ein Testkonzert",
                LocalDateTime.now().plusDays(30),
                "Karlsruhe"
        );

        TicketCategory ticketCategory = new TicketCategory(
                "VIP",
                BigDecimal.valueOf(100),
                50,
                concert
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now().plusMinutes(30),
                LocalDateTime.now().plusDays(7)
        );

        return new Ticket(order, ticketCategory);
    }

    private String basicAuthHeader() {
        String credentials = "admin:test-password";
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return "Basic " + encodedCredentials;
    }

    @Test
    void getTicketQrCodeReturnsPngForAdmin() throws Exception {
        Long concertId = 1L;
        String qrToken = "ticket-token";
        byte[] qrCodePng = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47
        };

        when(ticketService.generateQrCodePng(concertId, qrToken))
                .thenReturn(qrCodePng);

        mockMvc.perform(get("/api/admin/concerts/{concertId}/tickets/{qrToken}/qr-code", concertId, qrToken)
                        .header("Authorization", basicAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(qrCodePng));

        verify(ticketService).generateQrCodePng(concertId, qrToken);
    }

    @Test
    void getTicketQrCodeRequiresAdminLogin() throws Exception {
        mockMvc.perform(get("/api/admin/concerts/{concertId}/tickets/{qrToken}/qr-code", 1L, "ticket-token"))
                .andExpect(status().isUnauthorized());
    }
}