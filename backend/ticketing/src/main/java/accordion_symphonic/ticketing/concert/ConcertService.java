package accordion_symphonic.ticketing.concert;

import accordion_symphonic.ticketing.concert.dto.ConcertRequest;
import accordion_symphonic.ticketing.concert.dto.ConcertResponse;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;

    public ConcertService(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    public List<ConcertResponse> getPublishedConcerts() {
        return this.concertRepository.findByStatus(ConcertStatus.PUBLISHED)
                .stream()
                .map(ConcertResponse::fromEntity)
                .toList();
    }

    public ConcertResponse getPublishedConcertById(Long concertId) {
        return this.concertRepository.findByIdAndStatus(concertId, ConcertStatus.PUBLISHED)
                .map(ConcertResponse::fromEntity)
                .orElseThrow(() -> new ConcertNotFoundException(concertId));
    }

    public List<ConcertResponse> getAllConcerts() {
        return this.concertRepository.findAll()
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

        Concert savedConcert = this.concertRepository.save(concert);

        return ConcertResponse.fromEntity(savedConcert);
    }

    public ConcertResponse updateConcert(Long id, ConcertRequest request) {
        Concert concert = this.concertRepository.findById(id)
                .orElseThrow(() -> new ConcertNotFoundException(id));

        concert.updateConcert(request);

        Concert updatedConcert = this.concertRepository.save(concert);

        return ConcertResponse.fromEntity(updatedConcert);
    }

    public ConcertResponse publishConcert(Long id) {
        Concert concert = this.concertRepository.findById(id)
                .orElseThrow(() -> new ConcertNotFoundException(id));

        concert.publish();

        Concert publishedConcert = this.concertRepository.save(concert);

        return ConcertResponse.fromEntity(publishedConcert);
    }

    public ConcertResponse archiveConcert(Long id) {
        Concert concert = this.concertRepository.findById(id)
                .orElseThrow(() -> new ConcertNotFoundException(id));

        concert.archive();

        Concert archivedConcert = this.concertRepository.save(concert);

        return ConcertResponse.fromEntity(archivedConcert);
    }
}
