package accordion_symphonic.ticketing.common;

import accordion_symphonic.ticketing.concert.ConcertNotFoundException;
import accordion_symphonic.ticketing.availability.NotEnoughTicketsAvailableException;
import accordion_symphonic.ticketing.order.*;
import accordion_symphonic.ticketing.payment.InvalidPaymentWebhookSignatureException;
import accordion_symphonic.ticketing.payment.OrderCannotBePaidException;
import accordion_symphonic.ticketing.payment.UnsupportedPaymentStatusException;
import accordion_symphonic.ticketing.ticket.TicketIsNotValidException;
import accordion_symphonic.ticketing.ticket.TicketNotFoundException;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConcertNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleConcertNotFound(ConcertNotFoundException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(TicketCategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTicketCategoryNotFound(TicketCategoryNotFoundException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderNotFound(OrderNotFoundException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(OrderIsExpiredOrCancelledException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOrderIsExpiredOrCancelledException(
            OrderIsExpiredOrCancelledException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                exception.getMessage()
        );
    }

    @ExceptionHandler(OrderIsPaidOrExpiredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOrderIsPaidOrExpiredException(
            OrderIsPaidOrExpiredException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                exception.getMessage()
        );
    }

    @ExceptionHandler(TicketIsNotValidException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTicketIsNotValidException(
            TicketIsNotValidException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                exception.getMessage()
        );
    }

    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTicketNotFound(TicketNotFoundException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(NotEnoughTicketsAvailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleNotEnoughTicketsAvailable(NotEnoughTicketsAvailableException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateTicketCategoryException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateTicketCategory(DuplicateTicketCategoryException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidPaymentWebhookSignatureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidPaymentWebhookSignature(
            InvalidPaymentWebhookSignatureException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                exception.getMessage()
        );
    }

    @ExceptionHandler(UnsupportedPaymentStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnsupportedPaymentStatus(
            UnsupportedPaymentStatusException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                exception.getMessage()
        );
    }

    @ExceptionHandler(OrderCannotBePaidException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOrderCannotBePaid(
            OrderCannotBePaidException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                exception.getMessage());
    }

    @ExceptionHandler(TooManyTicketsInOrderException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTooManyTicketsInOrder(TooManyTicketsInOrderException exception) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ErrorCode.TOO_MANY_TICKETS_IN_ORDER,
                exception.getMessage()
        );
    }
}