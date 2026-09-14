package accordion_symphonic.ticketing.concert.exception;

public class ConcertCannotBeUpdatedException extends RuntimeException {
    public ConcertCannotBeUpdatedException(long ConcertId) {
        super("Concert with ID " + ConcertId + " cannot be updated.");
    }
}
