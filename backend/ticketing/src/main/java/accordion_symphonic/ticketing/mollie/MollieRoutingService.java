package accordion_symphonic.ticketing.mollie;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@ConditionalOnProperty(
        name = "mollie.routing.enabled",
        havingValue = "true"
)
public class MollieRoutingService {

    private final RestClient mollieClient;
    private final String apiKey;
    private final String partnerOrganizationId;

    private final MollieRoutingEventRepository routingEventRepository;

    public MollieRoutingService(
            @Value("${mollie.api-key}") String apiKey,
            @Value("${mollie.routing.partner-organization-id}")
            String partnerOrganizationId,
            MollieRoutingEventRepository routingEventRepository
    ) {
        this.apiKey = apiKey;
        this.partnerOrganizationId = partnerOrganizationId;

        this.mollieClient = RestClient.builder()
                .baseUrl("https://api.mollie.com/v2")
                .build();

        this.routingEventRepository = routingEventRepository;
    }

    @Transactional
    public void routePayment50_50(
            String paymentId,
            BigDecimal totalAmount
    ) {
        if (routingEventRepository.existsByPaymentId(paymentId)) {
            return;
        }

        BigDecimal partnerAmount = totalAmount
                .divide(
                        BigDecimal.valueOf(2),
                        2,
                        RoundingMode.DOWN
                );

        createRoute(
                paymentId,
                partnerOrganizationId,
                partnerAmount
        );

        routingEventRepository.save(
                new MollieRoutingEvent(
                        paymentId,
                        partnerAmount,
                        LocalDateTime.now()
                )
        );
    }

    private void createRoute(
            String paymentId,
            String organizationId,
            BigDecimal amount
    ) {
        String idempotencyKey =
                "route-" + paymentId + "-" + organizationId;

        MollieRouteRequest request =
                new MollieRouteRequest(
                        new Amount(
                                "EUR",
                                amount.toPlainString()
                        ),
                        new Destination(
                                "organization",
                                organizationId
                        ),
                        "50% Konzertanteil"
                );

        mollieClient.post()
                .uri(
                        "/payments/{paymentId}/routes",
                        paymentId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey
                )
                .header(
                        "Idempotency-Key",
                        idempotencyKey
                )
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private record MollieRouteRequest(
            Amount amount,
            Destination destination,
            String description
    ) {}

    private record Amount(
            String currency,
            String value
    ) {}

    private record Destination(
            String type,
            String organizationId
    ) {}
}