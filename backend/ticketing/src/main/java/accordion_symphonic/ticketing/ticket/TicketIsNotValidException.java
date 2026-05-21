package accordion_symphonic.ticketing.ticket;

public class TicketIsNotValidException extends RuntimeException {

    public TicketIsNotValidException() {
        super("Ticket is not valid");
    }
}
