package accordion_symphonic.ticketing.concert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
    List<Concert> findByStatusIn(
            Collection<ConcertStatus> statuses
    );

    Optional<Concert> findByIdAndStatus(
            Long id,
            ConcertStatus status
    );

    Optional<Concert> findByIdAndStatusIn(
            Long id,
            Collection<ConcertStatus> statuses
    );
}
