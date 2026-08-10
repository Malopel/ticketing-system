package accordion_symphonic.ticketing.concert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConcertRepository extends JpaRepository<Concert, Long> {

    List<Concert> findByStatus(ConcertStatus status);

    Optional<Concert> findByIdAndStatus(
            Long id,
            ConcertStatus status
    );
}
