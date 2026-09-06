package accordion_symphonic.ticketing.concert;

import accordion_symphonic.ticketing.concert.dto.ConcertRequest;
import accordion_symphonic.ticketing.concert.dto.ConcertResponse;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;

    public ConcertService(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    public List<ConcertResponse> getPublicConcerts() {
        return this.concertRepository.
                findByStatusIn(List.of(
                        ConcertStatus.PUBLISHED,
                        ConcertStatus.CANCELLED
                ))
                .stream()
                .map(ConcertResponse::fromEntity)
                .toList();
    }

    public ConcertResponse getPublicConcertById(Long concertId) {
        return this.concertRepository.findByIdAndStatusIn(
                        concertId,
                        List.of(ConcertStatus.PUBLISHED, ConcertStatus.CANCELLED)
                )
                .map(ConcertResponse::fromEntity)
                .orElseThrow(() -> new ConcertNotFoundException(concertId));
    }

    public List<ConcertResponse> getAllConcerts() {
        return this.concertRepository.findAll()
                .stream()
                .map(ConcertResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConcertResponse getConcertById(Long concertId) {
        return ConcertResponse.fromEntity(this.concertRepository.getReferenceById(concertId));
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

    @Transactional
    public ConcertResponse updateConcert(Long id, ConcertRequest request) {
        Concert concert = this.concertRepository.findById(id)
                .orElseThrow(() -> new ConcertNotFoundException(id));

        concert.updateConcert(request);

        return ConcertResponse.fromEntity(concert);
    }

    @Transactional
    public ConcertResponse publishConcert(Long id) {
        Concert concert = this.concertRepository.findById(id)
                .orElseThrow(() -> new ConcertNotFoundException(id));

        concert.publish();

        return ConcertResponse.fromEntity(concert);
    }

    @Transactional
    public ConcertResponse archiveConcert(Long id) {
        Concert concert = this.concertRepository.findById(id)
                .orElseThrow(() -> new ConcertNotFoundException(id));

        concert.archive();

        return ConcertResponse.fromEntity(concert);
    }

    @Transactional
    public ConcertResponse cancelConcert(Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ConcertNotFoundException(id));

        concert.cancel();

        return ConcertResponse.fromEntity(concert);
    }
}
