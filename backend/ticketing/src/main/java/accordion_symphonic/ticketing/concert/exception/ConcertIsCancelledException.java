package accordion_symphonic.ticketing.concert.exception;

public class ConcertIsCancelledException extends RuntimeException {
    public ConcertIsCancelledException(long concertId) {
        super("Concert " + concertId + " is cancelled");
    }
}
