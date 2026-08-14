package accordion_symphonic.ticketing.ticket.exception;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String qrToken) {
        super("Ticket with " + qrToken + " was not found");
    }
}
