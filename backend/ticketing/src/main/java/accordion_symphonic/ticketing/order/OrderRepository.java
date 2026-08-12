package accordion_symphonic.ticketing.order;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByConcertId(Long concertId);

    List<Order> findByCustomerEmail(String customerEmail);

    Optional<Order> findByIdAndConcertId(Long id, Long concertId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select o
        from Order o
        where o.id = :orderId
          and o.concert.id = :concertId
        """)
    Optional<Order> findByIdAndConcertIdForUpdate(
                    @Param("orderId") Long orderId,
                    @Param("concertId") Long concertId
            );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select o
        from Order o
        where o.id = :orderId
        """)
    Optional<Order> findByIdForUpdate(
            @Param("orderId") Long orderId
    );
}