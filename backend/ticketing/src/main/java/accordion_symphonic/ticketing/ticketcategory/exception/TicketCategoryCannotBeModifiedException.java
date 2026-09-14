package accordion_symphonic.ticketing.ticketcategory.exception;

public class TicketCategoryCannotBeModifiedException
        extends RuntimeException {

    public TicketCategoryCannotBeModifiedException(Long concertId) {
        super(
                "Ticket categories for concert "
                        + concertId
                        + " cannot be modified."
        );
    }
}