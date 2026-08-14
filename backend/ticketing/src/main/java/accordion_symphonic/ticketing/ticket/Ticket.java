package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.exception.TicketIsNotValidException;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_category_id")
    private TicketCategory ticketCategory;

    @Column(nullable = false, unique = true)
    private String qrToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    protected Ticket() { }

    public Ticket(Order order, TicketCategory ticketCategory) {
        this.order = order;
        this.ticketCategory = ticketCategory;
        this.qrToken = UUID.randomUUID().toString();
        this.status = TicketStatus.VALID;
    }

    public Long getId() {
        return this.id;
    }

    public Order getOrder() {
        return this.order;
    }

    public TicketCategory getTicketCategory() {
        return this.ticketCategory;
    }

    public String getQrToken() {
        return this.qrToken;
    }

    public TicketStatus getStatus() {
        return this.status;
    }

    public void useTicket() {
        if(this.status != TicketStatus.VALID) {
            throw new TicketIsNotValidException();
        }

        this.status = TicketStatus.USED;
    }

    public void cancelTicket() {
        if(this.status != TicketStatus.VALID) {
            throw new TicketIsNotValidException();
        }

        this.status = TicketStatus.CANCELLED;
    }
}
