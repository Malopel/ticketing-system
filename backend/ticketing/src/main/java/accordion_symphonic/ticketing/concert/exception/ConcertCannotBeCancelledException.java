package accordion_symphonic.ticketing.concert.exception;

public class ConcertCannotBeCancelledException extends RuntimeException {
    public ConcertCannotBeCancelledException(Long id) {
        super("Concert with id: " + id + "cannot be cancelled");
    }
}
