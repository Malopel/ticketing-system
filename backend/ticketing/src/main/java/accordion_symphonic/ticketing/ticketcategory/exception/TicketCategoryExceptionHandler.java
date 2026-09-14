package accordion_symphonic.ticketing.ticketcategory.exception;

import accordion_symphonic.ticketing.common.ErrorCode;
import accordion_symphonic.ticketing.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class TicketCategoryExceptionHandler {
    @ExceptionHandler(TicketCategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTicketCategoryNotFound(TicketCategoryNotFoundException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ErrorCode.TICKET_CATEGORY_NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(TicketCategoryCannotBeModifiedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTicketCategoryCannotBeModified(TicketCategoryCannotBeModifiedException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.TICKET_CATEGORY_CANNOT_BE_MODIFIED,
                exception.getMessage()
        );
    }
}
