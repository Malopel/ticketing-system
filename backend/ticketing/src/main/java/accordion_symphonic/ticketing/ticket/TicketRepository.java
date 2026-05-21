package accordion_symphonic.ticketing.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByOrderId(Long orderId);

    Optional<Ticket> findByQrTokenAndOrderConcertId(String qrToken, Long concertId);

    boolean existsByOrderId(Long orderId);
}