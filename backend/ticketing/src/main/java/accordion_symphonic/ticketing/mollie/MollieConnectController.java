package accordion_symphonic.ticketing.mollie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@ConditionalOnProperty(
        name = "mollie.connect.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/api/admin/mollie/connect")
public class MollieConnectController {

    private final MollieConnectStateService stateService;
    private final String clientId;
    private final String redirectUri;

    public MollieConnectController(
            MollieConnectStateService stateService,
            @Value("${mollie.connect.client-id}") String clientId,
            @Value("${mollie.connect.redirect-uri}") String redirectUri
    ) {
        this.stateService = stateService;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    @GetMapping
    public ResponseEntity<Void> connect() {
        String state = stateService.createState();

        URI authorizationUrl = UriComponentsBuilder
                .fromUriString("https://my.mollie.com/oauth2/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "organizations.read")
                .queryParam("state", state)
                .queryParam("locale", "de_DE")
                .build()
                .encode()
                .toUri();

        return ResponseEntity
                .status(302)
                .header(HttpHeaders.LOCATION, authorizationUrl.toString())
                .build();
    }
}