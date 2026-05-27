package accordion_symphonic.ticketing.order;

import jakarta.validation.constraints.NotNull;

public class DuplicateTicketCategoryException extends RuntimeException {

    public DuplicateTicketCategoryException(@NotNull Long ticketCategoryId) {
        super("Category with id: " + ticketCategoryId + " was already reserved");
    }
}
