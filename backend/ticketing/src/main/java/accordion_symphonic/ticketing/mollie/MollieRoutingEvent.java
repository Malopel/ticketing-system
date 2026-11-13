package accordion_symphonic.ticketing.mollie;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "mollie_routing_event",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mollie_routing_event_payment_id",
                columnNames = "payment_id"
        )
)
public class MollieRoutingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "payment_id",
            nullable = false
    )
    private String paymentId;

    @Column(
            name = "partner_amount",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal partnerAmount;

    @Column(
            name = "routed_at",
            nullable = false
    )
    private LocalDateTime routedAt;

    protected MollieRoutingEvent() {
    }

    public MollieRoutingEvent(
            String paymentId,
            BigDecimal partnerAmount,
            LocalDateTime routedAt
    ) {
        this.paymentId = paymentId;
        this.partnerAmount = partnerAmount;
        this.routedAt = routedAt;
    }
}
