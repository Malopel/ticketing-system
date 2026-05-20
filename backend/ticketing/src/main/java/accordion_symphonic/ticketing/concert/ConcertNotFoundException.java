package accordion_symphonic.ticketing.concert;

public class ConcertNotFoundException extends RuntimeException {

    public ConcertNotFoundException(Long id) {
        super("Concert with id " + id + " not found");
    }
}