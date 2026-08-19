package accordion_symphonic.ticketing.ticket.exception;

import accordion_symphonic.ticketing.common.ErrorCode;
import accordion_symphonic.ticketing.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class TicketExceptionHandler {

    @ExceptionHandler(TicketIsNotValidException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTicketIsNotValid(
            TicketIsNotValidException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.INVALID_TICKET,
                exception.getMessage()
        );
    }

    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTicketNotFound(
            TicketNotFoundException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ErrorCode.TICKET_NOT_FOUND,
                exception.getMessage()
        );
    }
}