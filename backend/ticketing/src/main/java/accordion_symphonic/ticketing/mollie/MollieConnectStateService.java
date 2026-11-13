package accordion_symphonic.ticketing.mollie;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MollieConnectStateService {

    private final Map<String, Instant> states = new ConcurrentHashMap<>();

    public String createState() {
        String state = UUID.randomUUID().toString();
        states.put(state, Instant.now().plusSeconds(600));
        return state;
    }

    public boolean consumeState(String state) {
        Instant expiresAt = states.remove(state);

        return expiresAt != null
                && expiresAt.isAfter(Instant.now());
    }
}
