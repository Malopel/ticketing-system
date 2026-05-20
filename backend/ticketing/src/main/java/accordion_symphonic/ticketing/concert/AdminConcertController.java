package accordion_symphonic.ticketing.concert;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/concerts")
public class AdminConcertController {

    private final ConcertService concertService;

    public AdminConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }

    @GetMapping
    public List<ConcertResponse> getConcerts() {
        return concertService.getAllConcerts();
    }

    @PostMapping
    public ConcertResponse addConcert(@RequestBody ConcertRequest concertRequest) {
        return concertService.createConcert(concertRequest);
    }

    @PutMapping("/{id}")
    public ConcertResponse updateConcert(@RequestBody ConcertRequest concertRequest, @PathVariable Long id) {
        return concertService.updateConcert(id, concertRequest);
    }

    @PatchMapping("/{id}/publish")
    public ConcertResponse publishConcert(@PathVariable Long id) {
        return concertService.publishConcert(id);
    }

    @PatchMapping("/{id}/archive")
    public ConcertResponse archiveConcert(@PathVariable Long id) {
        return concertService.archiveConcert(id);
    }
}
