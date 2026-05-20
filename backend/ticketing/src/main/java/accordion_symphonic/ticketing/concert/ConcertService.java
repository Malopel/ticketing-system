package accordion_symphonic.ticketing.concert;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;

    public ConcertService(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    public List<ConcertResponse> getPublishedConcerts() {
        return concertRepository.findByStatus(ConcertStatus.PUBLISHED)
                .stream()
                .map(ConcertResponse::fromEntity)
                .toList();
    }

    public ConcertResponse createConcert(ConcertRequest request) {
        Concert concert = new Concert(
                request.title(),
                request.description(),
                request.startTime(),
                request.location()
        );

        Concert savedConcert = concertRepository.save(concert);

        return ConcertResponse.fromEntity(savedConcert);
    }
}
