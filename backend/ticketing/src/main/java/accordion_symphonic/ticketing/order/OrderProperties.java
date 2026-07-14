package accordion_symphonic.ticketing.order;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ticketing.order")
public record OrderProperties(Duration reservationDuration) {

    public OrderProperties {
        if (reservationDuration == null) {
            reservationDuration = Duration.ofMinutes(30);
        }

        if (reservationDuration.isZero() || reservationDuration.isNegative()) {
            throw new IllegalArgumentException("reservationDuration must be positive");
        }
    }
}