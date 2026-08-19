package accordion_symphonic.ticketing.concert.exception;

import accordion_symphonic.ticketing.common.ErrorCode;
import accordion_symphonic.ticketing.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ConcertExceptionHandler {

    @ExceptionHandler(ConcertNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleConcertNotFound(ConcertNotFoundException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ErrorCode.CONCERT_NOT_FOUND,
                exception.getMessage()
        );
    }
}
