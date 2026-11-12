package accordion_symphonic.ticketing.payment.provider;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.payment.PaymentProvider;
import accordion_symphonic.ticketing.payment.PaymentSession;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import org.springframework.http.HttpHeaders;

import java.math.RoundingMode;
import java.util.Map;

@Component
@ConditionalOnProperty(
        name = "ticketing.payment.provider",
        havingValue = "mollie"
)
public class MolliePaymentProvider implements PaymentProvider {

    private final RestClient restClient;
    private final String frontendBaseUrl;
    private final String backendBaseUrl;

    public MolliePaymentProvider(
            @Value("${mollie.api-key}") String apiKey,
            @Value("${ticketing.frontend.base-url}") String frontendBaseUrl,
            @Value("${ticketing.backend.base-url}") String backendBaseUrl
    ) {
        this.frontendBaseUrl = frontendBaseUrl;
        this.backendBaseUrl = backendBaseUrl;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.mollie.com/v2")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    @Override
    public PaymentSession createPayment(Order order) {

        Map<String, Object> body = getBody(order);

        MolliePaymentResponse response =
                restClient.post()
                        .uri("/payments")
                        .body(body)
                        .retrieve()
                        .body(MolliePaymentResponse.class);

        if (response == null
                || response.id() == null
                || response.links() == null
                || response.links().checkout() == null
                || response.links().checkout().href() == null
        ) {
            throw new IllegalStateException(
                    "Mollie returned an invalid payment response."
            );
        }

        return new PaymentSession(
                response.id(),
                response.links().checkout().href()
        );
    }

    private @NonNull Map<String, Object> getBody(Order order) {
        String amountValue =
                order.getTotalAmount()
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString();

        return Map.of(
                "amount", Map.of(
                        "currency", "EUR",
                        "value", amountValue
                ),
                "description", "Bestellung " + order.getPublicId(),

                "redirectUrl", frontendBaseUrl + "/orders/" + order.getId(),

                "webhookUrl", backendBaseUrl + "/api/payments/mollie/webhook",

                "metadata", Map.of(
                        "orderId", order.getId()
                )
        );
    }

    private record MolliePaymentResponse(
            String id,

            @JsonProperty("_links")
            MollieLinks links
    ) {}

    private record MollieLinks(
            MollieLink checkout
    ) {}

    private record MollieLink(
            String href
    ) {}
}
