package accordion_symphonic.ticketing.ticketcategory;

public class TicketCategoryNotFoundException extends RuntimeException {

    public TicketCategoryNotFoundException(Long id) {
        super("TicketCategory with id " + id + " not found");
    }
}
