package accordion_symphonic.ticketing.ticket.batches;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class PrintedTicketBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Concert concert;

    @OneToOne(optional = false)
    private Order order;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected PrintedTicketBatch() {
    }

    public PrintedTicketBatch(
            Concert concert,
            Order order,
            String label
    ) {
        this.concert = concert;
        this.order = order;
        this.label = label;
        this.createdAt = LocalDateTime.now();
    }
}