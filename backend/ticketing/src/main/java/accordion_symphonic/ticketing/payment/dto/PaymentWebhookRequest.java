package accordion_symphonic.ticketing.payment.dto;

import accordion_symphonic.ticketing.payment.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentWebhookRequest(
        @NotBlank
        String eventId,

        @NotNull
        Long orderId,

        @NotNull
        PaymentStatus status
) {
}
