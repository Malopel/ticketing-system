package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.order.Order;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "payment_event",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_event_id",
                        columnNames = "event_id"
                )
        }
)
public class PaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    protected PaymentEvent() { }

    public PaymentEvent(
            String eventId,
            Order order,
            PaymentStatus paymentStatus,
            LocalDateTime receivedAt,
            LocalDateTime processedAt
    ) {
        this.eventId = eventId;
        this.order = order;
        this.paymentStatus = paymentStatus;
        this.receivedAt = receivedAt;
        this.processedAt = processedAt;
    }

}
