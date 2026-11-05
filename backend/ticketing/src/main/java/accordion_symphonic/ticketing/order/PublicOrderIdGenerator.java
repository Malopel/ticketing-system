package accordion_symphonic.ticketing.order;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PublicOrderIdGenerator {
    public String generate() {
        String random = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        return "AS-%s-%s-%s".formatted(
                random.substring(0, 4),
                random.substring(4, 8),
                random.substring(8, 12)
        );
    }
}
