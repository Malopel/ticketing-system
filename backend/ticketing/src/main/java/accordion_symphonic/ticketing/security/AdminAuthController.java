package accordion_symphonic.ticketing.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @GetMapping("/check")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkAuthentication() {
    }
}
