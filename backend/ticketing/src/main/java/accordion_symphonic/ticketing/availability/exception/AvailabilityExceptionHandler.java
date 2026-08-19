package accordion_symphonic.ticketing.availability.exception;

import accordion_symphonic.ticketing.common.ErrorCode;
import accordion_symphonic.ticketing.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class AvailabilityExceptionHandler {
    @ExceptionHandler(NotEnoughTicketsAvailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleNotEnoughTicketsAvailable(NotEnoughTicketsAvailableException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.NOT_ENOUGH_TICKETS_AVAILABLE,
                exception.getMessage()
        );
    }
}
