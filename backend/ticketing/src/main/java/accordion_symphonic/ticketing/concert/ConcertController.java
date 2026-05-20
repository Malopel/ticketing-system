package accordion_symphonic.ticketing.concert;

import org.springframework.web.bind.annotation.*;

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
}
