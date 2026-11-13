package accordion_symphonic.ticketing.mollie;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@ConditionalOnProperty(
        name = "mollie.connect.enabled",
        havingValue = "true"
)
@Service
public class MollieConnectService {

    private final MollieConnectedAccountRepository connectedAccountRepository;

    private final RestClient oauthClient;
    private final RestClient mollieClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public MollieConnectService(
            @Value("${mollie.connect.client-id}") String clientId,
            @Value("${mollie.connect.client-secret}") String clientSecret,
            @Value("${mollie.connect.redirect-uri}") String redirectUri,
            MollieConnectedAccountRepository connectedAccountRepository
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.connectedAccountRepository = connectedAccountRepository;

        this.oauthClient = RestClient.builder()
                .baseUrl("https://api.mollie.com")
                .build();

        this.mollieClient = RestClient.builder()
                .baseUrl("https://api.mollie.com/v2")
                .build();
    }

    @Transactional
    public MollieConnectedOrganization connect(String authorizationCode) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", authorizationCode);
        form.add("redirect_uri", redirectUri);

        MollieTokenResponse tokenResponse = oauthClient.post()
                .uri("/oauth2/tokens")
                .headers(headers -> {
                    headers.setBasicAuth(clientId, clientSecret);
                    headers.setContentType(
                            MediaType.APPLICATION_FORM_URLENCODED
                    );
                })
                .body(form)
                .retrieve()
                .body(MollieTokenResponse.class);

        if (tokenResponse == null
                || tokenResponse.accessToken() == null) {
            throw new IllegalStateException(
                    "Mollie OAuth token response was empty"
            );
        }

        MollieOrganizationResponse organization =
                mollieClient.get()
                        .uri("/organizations/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + tokenResponse.accessToken()
                        )
                        .retrieve()
                        .body(MollieOrganizationResponse.class);

        if (organization == null) {
            throw new IllegalStateException(
                    "Mollie organization response was empty"
            );
        }

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(tokenResponse.expiresIn());

        MollieConnectedAccount connectedAccount =
                connectedAccountRepository
                        .findByOrganizationId(organization.id())
                        .map(existing -> {
                            existing.updateCredentials(
                                    tokenResponse.refreshToken(),
                                    expiresAt
                            );
                            return existing;
                        })
                        .orElseGet(() ->
                                new MollieConnectedAccount(
                                        organization.id(),
                                        tokenResponse.refreshToken(),
                                        expiresAt
                                )
                        );

        connectedAccountRepository.save(connectedAccount);

        return new MollieConnectedOrganization(
                organization.id(),
                organization.name(),
                tokenResponse.accessToken(),
                tokenResponse.refreshToken()
        );
    }

    private record MollieTokenResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("access_token")
            String accessToken,

            @com.fasterxml.jackson.annotation.JsonProperty("refresh_token")
            String refreshToken,

            @com.fasterxml.jackson.annotation.JsonProperty("expires_in")
            long expiresIn,

            @com.fasterxml.jackson.annotation.JsonProperty("token_type")
            String tokenType,

            String scope
    ) {
    }

    private record MollieOrganizationResponse(
            String id,
            String name
    ) {
    }
}