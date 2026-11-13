package accordion_symphonic.ticketing.mollie;

public record MollieConnectedOrganization(
        String id,
        String name,
        String accessToken,
        String refreshToken
) {
}