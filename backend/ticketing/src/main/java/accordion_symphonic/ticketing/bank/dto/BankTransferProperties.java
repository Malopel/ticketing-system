package accordion_symphonic.ticketing.bank.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ticketing.bank-transfer")
public record BankTransferProperties(
        boolean enabled,
        String recipient,
        String iban,
        String bic,
        long reservationHours
) {
}