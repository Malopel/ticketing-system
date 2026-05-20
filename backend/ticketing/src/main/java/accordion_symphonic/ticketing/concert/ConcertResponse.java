package accordion_symphonic.ticketing.concert;

import java.time.LocalDateTime;

public record ConcertResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startTime,
        String location,
        ConcertStatus status
) {
    public static ConcertResponse fromEntity(Concert concert) {
        return new ConcertResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getDescription(),
                concert.getStartTime(),
                concert.getLocation(),
                concert.getStatus()
        );
    }
}