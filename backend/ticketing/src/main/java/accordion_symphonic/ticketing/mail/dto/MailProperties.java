package accordion_symphonic.ticketing.mail.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ticketing.mail")
public record MailProperties(String from) {
}
