package accordion_symphonic.ticketing.ticketcategory;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertNotFoundException;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final ConcertRepository concertRepository;

    public TicketCategoryService(
            TicketCategoryRepository ticketCategoryRepository,
            ConcertRepository concertRepository
    ) {
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.concertRepository = concertRepository;
    }

    public List<TicketCategoryResponse> getCategoriesForConcert(Long concertId) {
        if (!concertRepository.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        return ticketCategoryRepository.findByConcertId(concertId)
                .stream()
                .map(TicketCategoryResponse::fromEntity)
                .toList();
    }

    public TicketCategoryResponse createCategory(Long concertId, TicketCategoryRequest request) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new ConcertNotFoundException(concertId));

        TicketCategory category = new TicketCategory(
                request.name(),
                request.price(),
                request.capacity(),
                concert
        );

        TicketCategory savedCategory = ticketCategoryRepository.save(category);

        return TicketCategoryResponse.fromEntity(savedCategory);
    }

    public TicketCategoryResponse updateCategory(
            Long concertId,
            Long categoryId,
            TicketCategoryRequest request
    ) {
        TicketCategory category = ticketCategoryRepository
                .findByIdAndConcertId(categoryId, concertId)
                .orElseThrow(() -> new TicketCategoryNotFoundException(categoryId));

        category.update(
                request.name(),
                request.price(),
                request.capacity()
        );

        TicketCategory savedCategory = ticketCategoryRepository.save(category);

        return TicketCategoryResponse.fromEntity(savedCategory);
    }

    public void deleteCategory(Long concertId, Long categoryId) {
        TicketCategory category = ticketCategoryRepository
                .findByIdAndConcertId(categoryId, concertId)
                .orElseThrow(() -> new TicketCategoryNotFoundException(categoryId));

        ticketCategoryRepository.delete(category);
    }
}