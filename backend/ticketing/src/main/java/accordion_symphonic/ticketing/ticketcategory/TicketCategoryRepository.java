package accordion_symphonic.ticketing.ticketcategory;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {

    List<TicketCategory> findByConcertId(Long concertId);

    Optional<TicketCategory> findByIdAndConcertId(Long id, Long concertId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select category
            from TicketCategory category
            where category.id = :id
            and category.concert.id = :concertId
            """)
    Optional<TicketCategory> findByIdAndConcertIdForUpdate(
            @Param("id") Long id,
            @Param("concertId") Long concertId
    );
}