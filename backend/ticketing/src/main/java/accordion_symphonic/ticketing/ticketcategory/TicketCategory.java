package accordion_symphonic.ticketing.ticketcategory;

import accordion_symphonic.ticketing.concert.Concert;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal price;

    private int capacity;

    @ManyToOne(optional = false)
    private Concert concert;

    protected TicketCategory() {
    }

    public TicketCategory(String name, BigDecimal price, int capacity, Concert concert) {
        this.name = name;
        this.price = price;
        this.capacity = capacity;
        this.concert = concert;
    }

    public void update(String name, BigDecimal price, int capacity) {
        this.name = name;
        this.price = price;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getCapacity() {
        return capacity;
    }

    public Concert getConcert() {
        return concert;
    }
}
