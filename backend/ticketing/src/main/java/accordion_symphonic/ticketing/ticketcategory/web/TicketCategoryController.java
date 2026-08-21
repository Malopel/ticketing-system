package accordion_symphonic.ticketing.ticketcategory.web;

import accordion_symphonic.ticketing.ticketcategory.TicketCategoryService;
import accordion_symphonic.ticketing.ticketcategory.dto.TicketCategoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/concerts/{concertId}/ticket-categories")
public class TicketCategoryController {

    private final TicketCategoryService ticketCategoryService;

    public TicketCategoryController(TicketCategoryService ticketCategoryService) {
        this.ticketCategoryService = ticketCategoryService;
    }

    @GetMapping
    public List<TicketCategoryResponse> getCategoriesForConcert(
            @PathVariable("concertId") Long concertId
    ) {
        return ticketCategoryService.getCategoriesForConcert(concertId);
    }
}
