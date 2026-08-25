package accordion_symphonic.ticketing.ticketcategory.web;

import accordion_symphonic.ticketing.ticketcategory.TicketCategoryService;
import accordion_symphonic.ticketing.ticketcategory.dto.TicketCategoryRequest;
import accordion_symphonic.ticketing.ticketcategory.dto.TicketCategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}/ticket-categories")
public class AdminTicketCategoryController {

    private final TicketCategoryService ticketCategoryService;

    public AdminTicketCategoryController(TicketCategoryService ticketCategoryService) {
        this.ticketCategoryService = ticketCategoryService;
    }

    @GetMapping
    public List<TicketCategoryResponse> getCategories(
            @PathVariable("concertId") Long concertId
    ) {
        return ticketCategoryService.getCategoriesForAdmin(concertId);
    }

    @PostMapping
    public TicketCategoryResponse createCategory(
            @PathVariable("concertId") Long concertId,
            @Valid @RequestBody TicketCategoryRequest request
    ) {
        return ticketCategoryService.createCategory(concertId, request);
    }

    @PutMapping("/{categoryId}")
    public TicketCategoryResponse updateCategory(
            @PathVariable("concertId") Long concertId,
            @PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody TicketCategoryRequest request
    ) {
        return ticketCategoryService.updateCategory(concertId, categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @PathVariable("concertId") Long concertId,
            @PathVariable("categoryId") Long categoryId
    ) {
        ticketCategoryService.deleteCategory(concertId, categoryId);
    }
}