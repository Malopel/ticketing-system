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

    @ExceptionHandler(ConcertCannotBeCancelledException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConcertCannotBeCancelled(ConcertCannotBeCancelledException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.CONCERT_CANNOT_BE_CANCELLED,
                exception.getMessage()
        );
    }

    @ExceptionHandler(ConcertCannotBeArchivedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConcertCannotBeArchived(ConcertCannotBeArchivedException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.CONCERT_CANNOT_BE_ARCHIVED,
                exception.getMessage()
        );
    }

    @ExceptionHandler(ConcertCannotBePublishedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConcertCannotBePublished(ConcertCannotBePublishedException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.CONCERT_CANNOT_BE_PUBLISHED,
                exception.getMessage()
        );
    }

    @ExceptionHandler(ConcertIsCancelledException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse handleConcertIsCancelled(ConcertIsCancelledException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_ACCEPTABLE.value(),
                "Not acceptable",
                ErrorCode.CONCERT_IS_CANCELLED,
                exception.getMessage()
        );
    }
}
