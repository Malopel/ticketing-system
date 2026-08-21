package accordion_symphonic.ticketing.concert;

import accordion_symphonic.ticketing.concert.dto.ConcertResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequestMapping("api/concerts")
public class ConcertController {

    private final ConcertService concertService;

    public ConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }

    @GetMapping
    public List<ConcertResponse> getConcerts() {
        return concertService.getPublishedConcerts();
    }

    @GetMapping("/{concertId}")
    public ConcertResponse getPublishedConcertById(
            @PathVariable("concertId") Long concertId
    ) {
        return concertService.getPublishedConcertById(concertId);
    }
}
