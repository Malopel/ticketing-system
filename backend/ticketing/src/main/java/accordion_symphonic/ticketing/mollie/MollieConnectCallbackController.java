package accordion_symphonic.ticketing.mollie;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ConditionalOnProperty(
        name = "mollie.connect.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/api/mollie/connect")
public class MollieConnectCallbackController {

    private final MollieConnectStateService stateService;
    private final MollieConnectService connectService;

    public MollieConnectCallbackController(
            MollieConnectStateService stateService,
            MollieConnectService connectService
    ) {
        this.stateService = stateService;
        this.connectService = connectService;
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        if (!stateService.consumeState(state)) {
            return ResponseEntity
                    .badRequest()
                    .body("Ungültiger oder abgelaufener OAuth-State.");
        }

        MollieConnectedOrganization organization =
                connectService.connect(code);

        return ResponseEntity.ok(
                "Mollie-Verbindung erfolgreich: "
                        + organization.name()
                        + " (" + organization.id() + ")"
        );
    }
}