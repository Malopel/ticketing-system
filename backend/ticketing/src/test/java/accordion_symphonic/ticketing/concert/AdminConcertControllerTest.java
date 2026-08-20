package accordion_symphonic.ticketing.concert;

import accordion_symphonic.ticketing.common.CommonExceptionHandler;
import accordion_symphonic.ticketing.concert.exception.ConcertExceptionHandler;
import accordion_symphonic.ticketing.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminConcertController.class)
@Import({
        SecurityConfig.class,
        ConcertExceptionHandler.class,
        CommonExceptionHandler.class
})
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password"
})
class AdminConcertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConcertService concertService;

    @Test
    void createConcertReturnsValidationErrorForInvalidRequest()
            throws Exception {

        mockMvc.perform(
                        post("/api/admin/concerts")
                                .header(
                                        "Authorization",
                                        basicAuthHeader()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "title": "",
                                  "description": "Test",
                                  "startTime": "2020-01-01T20:00:00",
                                  "location": "Karlsruhe"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .exists()
                );

        verifyNoInteractions(concertService);
    }

    private String basicAuthHeader() {
        String credentials = "admin:test-password";

        String encodedCredentials =
                Base64.getEncoder()
                        .encodeToString(
                                credentials.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        return "Basic " + encodedCredentials;
    }
}