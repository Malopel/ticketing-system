package accordion_symphonic.ticketing.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record OrderRequest(

        @Email
        @NotBlank
        String customerEmail,

        @NotEmpty
        List<@Valid OrderItemRequest> items
) {
}
