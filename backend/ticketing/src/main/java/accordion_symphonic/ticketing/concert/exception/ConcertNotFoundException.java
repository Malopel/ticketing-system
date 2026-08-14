package accordion_symphonic.ticketing.concert.exception;

public class ConcertNotFoundException extends RuntimeException {

    public ConcertNotFoundException(Long id) {
        super("Concert with id " + id + " not found");
    }
}