package accordion_symphonic.ticketing.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByConcertId(Long concertId);

    List<Order> findByCustomerEmail(String customerEmail);

    Optional<Order> findByIdAndConcertId(Long id, Long concertId);
}