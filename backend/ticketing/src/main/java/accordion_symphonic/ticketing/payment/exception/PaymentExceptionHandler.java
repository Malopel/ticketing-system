package accordion_symphonic.ticketing.payment.exception;

import accordion_symphonic.ticketing.common.ErrorCode;
import accordion_symphonic.ticketing.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler(InvalidPaymentWebhookSignatureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidPaymentWebhookSignature(
            InvalidPaymentWebhookSignatureException exception
    ) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ErrorCode.INVALID_PAYMENT_SIGNATURE,
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
                ErrorCode.ORDER_CANNOT_BE_PAID,
                exception.getMessage()
        );
    }
}