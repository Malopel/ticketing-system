package accordion_symphonic.ticketing.availability;

public class NotEnoughTicketsAvailableException extends RuntimeException {

    public NotEnoughTicketsAvailableException(Long ticketCategoryId, int available) {
        super("Not enough tickets available for category " + ticketCategoryId + ". Available: " + available);
    }
}
