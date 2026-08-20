package accordion_symphonic.ticketing.concert.exception;

public class ConcertCannotBeArchivedException extends RuntimeException {
    public ConcertCannotBeArchivedException(long concertId) {
        super("Concert " + concertId + " cannot be archived");
    }
}
