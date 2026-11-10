package accordion_symphonic.ticketing.ticket.batches;

import accordion_symphonic.ticketing.ticket.batches.dto.TicketBatchResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrintedTicketBatchRepository
        extends JpaRepository<PrintedTicketBatch, Long> {

    Optional<PrintedTicketBatch> findByIdAndConcertId(
            Long id,
            Long concertId
    );

    List<TicketBatchResponse> findByConcertId(Long concertId);
}