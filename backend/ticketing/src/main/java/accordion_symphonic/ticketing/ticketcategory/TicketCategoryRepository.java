package accordion_symphonic.ticketing.ticketcategory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {

    List<TicketCategory> findByConcertId(Long concertId);

    Optional<TicketCategory> findByIdAndConcertId(Long id, Long concertId);
}