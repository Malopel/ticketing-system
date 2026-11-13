package accordion_symphonic.ticketing.mollie;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MollieRoutingEventRepository
        extends JpaRepository<MollieRoutingEvent, Long> {

    boolean existsByPaymentId(String paymentId);
}