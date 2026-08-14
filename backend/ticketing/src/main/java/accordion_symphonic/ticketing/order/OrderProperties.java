package accordion_symphonic.ticketing.order;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ticketing.order")
public record OrderProperties(
        Duration reservationDuration,
        Integer maxTicketsPerOrder,
        Duration paymentDuration
) {

    public OrderProperties {
        if (reservationDuration == null) {
            reservationDuration = Duration.ofMinutes(30);
        }

        if (maxTicketsPerOrder == null) {
            maxTicketsPerOrder = 10;
        }

        if (reservationDuration.isZero() || reservationDuration.isNegative()) {
            throw new IllegalArgumentException("reservationDuration must be positive");
        }

        if (maxTicketsPerOrder <= 0) {
            throw new IllegalArgumentException("maxTicketsPerOrder must at least one");
        }

        if (paymentDuration == null) {
            paymentDuration = Duration.ofMinutes(20);
        }

        if (paymentDuration.isZero() || paymentDuration.isNegative()) {
            throw new IllegalArgumentException("paymentDuration must be positive");
        }
    }
}