package accordion_symphonic.ticketing.common;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message
) {

    public ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message
    ) {
        this(
                timestamp,
                status,
                error,
                ErrorCode.UNKNOWN_ERROR,
                message
        );
    }
}