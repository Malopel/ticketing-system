package accordion_symphonic.ticketing.ticket;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByOrderId(Long orderId);

    Optional<Ticket> findByQrTokenAndOrderConcertId(String qrToken, Long concertId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select t
            from Ticket t
            where t.qrToken = :qrToken
              and t.order.concert.id = :concertId
            """)
    Optional<Ticket> findByQrTokenAndOrderConcertIdForUpdate(
            @Param("qrToken") String qrToken,
            @Param("concertId") Long concertId
    );

    boolean existsByOrderId(Long orderId);
}