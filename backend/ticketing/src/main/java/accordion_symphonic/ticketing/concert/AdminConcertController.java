package accordion_symphonic.ticketing.concert;

import accordion_symphonic.ticketing.concert.dto.ConcertRequest;
import accordion_symphonic.ticketing.concert.dto.ConcertResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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
    public ConcertResponse addConcert(@Valid @RequestBody ConcertRequest concertRequest) {
        return concertService.createConcert(concertRequest);
    }

    @PutMapping("/{concertId}")
    public ConcertResponse updateConcert(@Valid @RequestBody ConcertRequest concertRequest, @PathVariable Long concertId) {
        return concertService.updateConcert(concertId, concertRequest);
    }

    @PatchMapping("/{concertId}/publish")
    public ConcertResponse publishConcert(@PathVariable Long concertId) {
        return concertService.publishConcert(concertId);
    }

    @PatchMapping("/{concertId}/archive")
    public ConcertResponse archiveConcert(@PathVariable Long concertId) {
        return concertService.archiveConcert(concertId);
    }

    @PatchMapping("/{concertId}/cancel")
    public ConcertResponse cancelConcert(
            @PathVariable Long concertId
    ) {
        return concertService.cancelConcert(concertId);
    }
}
