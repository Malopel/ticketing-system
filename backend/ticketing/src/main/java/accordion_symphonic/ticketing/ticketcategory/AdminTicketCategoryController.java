package accordion_symphonic.ticketing.ticketcategory;

import accordion_symphonic.ticketing.ticketcategory.dto.TicketCategoryRequest;
import accordion_symphonic.ticketing.ticketcategory.dto.TicketCategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/admin/concerts/{concertId}/ticket-categories")
public class AdminTicketCategoryController {

    private final TicketCategoryService ticketCategoryService;

    public AdminTicketCategoryController(TicketCategoryService ticketCategoryService) {
        this.ticketCategoryService = ticketCategoryService;
    }

    @PostMapping
    public TicketCategoryResponse createCategory(
            @PathVariable Long concertId,
            @Valid @RequestBody TicketCategoryRequest request
    ) {
        return ticketCategoryService.createCategory(concertId, request);
    }

    @PutMapping("/{categoryId}")
    public TicketCategoryResponse updateCategory(
            @PathVariable Long concertId,
            @PathVariable Long categoryId,
            @Valid @RequestBody TicketCategoryRequest request
    ) {
        return ticketCategoryService.updateCategory(concertId, categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long concertId, @PathVariable Long categoryId) {
        ticketCategoryService.deleteCategory(concertId, categoryId);
    }
}