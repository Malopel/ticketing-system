package accordion_symphonic.ticketing;

import accordion_symphonic.ticketing.concert.AdminConcertController;
import accordion_symphonic.ticketing.concert.ConcertController;
import accordion_symphonic.ticketing.concert.ConcertService;
import accordion_symphonic.ticketing.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ConcertController.class, AdminConcertController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password"
})
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ConcertService concertService;

    @Test
    void publicConcertEndpointIsAccessibleWithoutAuthentication() throws Exception {
        when(concertService.getPublishedConcerts()).thenReturn(List.of());

        mockMvc.perform(get("/api/concerts"))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpointRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/admin/concerts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointAcceptsConfiguredAdmin() throws Exception {
        when(concertService.getAllConcerts()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/concerts")
                        .with(httpBasic("admin", "test-password")))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void adminEndpointRejectsNonAdminUser() throws Exception {
        mockMvc.perform(get("/api/admin/concerts"))
                .andExpect(status().isForbidden());
    }
}