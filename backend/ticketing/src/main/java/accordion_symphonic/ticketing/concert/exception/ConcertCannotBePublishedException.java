package accordion_symphonic.ticketing.concert.exception;

public class ConcertCannotBePublishedException extends RuntimeException {
    public ConcertCannotBePublishedException(long concertId) {
        super("Concert " + concertId + "cannot be published.");
    }
}
