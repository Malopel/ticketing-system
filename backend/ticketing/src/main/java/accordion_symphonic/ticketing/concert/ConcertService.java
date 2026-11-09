package accordion_symphonic.ticketing.concert;

import accordion_symphonic.ticketing.concert.dto.ConcertRequest;
import accordion_symphonic.ticketing.concert.dto.ConcertResponse;

import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ResourcePatternResolver resourcePatternResolver;

    public ConcertService(
            ConcertRepository concertRepository,
            ResourcePatternResolver resourcePatternResolver
    ) {
        this.concertRepository = concertRepository;
        this.resourcePatternResolver = resourcePatternResolver;
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
        Concert concert = this.concertRepository.findById(id)
                .orElseThrow(() -> new ConcertNotFoundException(id));

        concert.cancel();

        return ConcertResponse.fromEntity(concert);
    }

    public String getTicketTemplate(Long id) {
        Concert concert = this.concertRepository.findById(id)
                .orElseThrow(() -> new ConcertNotFoundException(id));

        return concert.getTicketTemplateFile();
    }

    @Transactional
    public String updateTicketFileTemplate(Long id, String fileName) {
        Concert concert = this.concertRepository.findById(id)
                .orElseThrow(() -> new ConcertNotFoundException(id));

        if (!getAllTicketTemplates().contains(fileName)) {
            throw new IllegalArgumentException(
                    "Unknown ticket template: " + fileName
            );
        }

        concert.updateTicketTemplateFile(fileName);

        return concert.getTicketTemplateFile();
    }

    public List<String> getAllTicketTemplates() {
        try {
            Resource[] resources =
                    resourcePatternResolver.getResources(
                            "classpath*:pdf/tickets/*.pdf"
                    );

            return Arrays.stream(resources)
                    .map(Resource::getFilename)
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Ticketvorlagen konnten nicht geladen werden.",
                    exception
            );
        }
    }
}