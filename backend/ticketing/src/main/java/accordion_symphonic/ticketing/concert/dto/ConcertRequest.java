package accordion_symphonic.ticketing.concert.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ConcertRequest(
        @NotBlank String title,
        String description,
        @NotNull @Future LocalDateTime startTime,
        @NotBlank String location
) {
}
