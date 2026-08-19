package accordion_symphonic.ticketing.order.exception;

import accordion_symphonic.ticketing.common.ErrorCode;
import accordion_symphonic.ticketing.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderNotFound(
            OrderNotFoundException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ErrorCode.ORDER_NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(OrderCannotBeCancelledException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOrderCannotBeCancelled(
            OrderCannotBeCancelledException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateTicketCategoryException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateTicketCategory(
            DuplicateTicketCategoryException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.DUPLICATE_TICKET_CATEGORY,
                exception.getMessage()
        );
    }

    @ExceptionHandler(TooManyTicketsInOrderException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTooManyTicketsInOrder(
            TooManyTicketsInOrderException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.TOO_MANY_TICKETS_IN_ORDER,
                exception.getMessage()
        );
    }

    @ExceptionHandler(OrderHasNoTicketsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOrderHasNoTickets(
            OrderHasNoTicketsException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.ORDER_HAS_NO_TICKETS,
                exception.getMessage()
        );
    }
}