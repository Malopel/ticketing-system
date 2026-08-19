package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.concert.Concert;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "concert_id")
    private Concert concert;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false, unique = true, length = 64)
    private String accessTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime paidAt;

    private LocalDateTime paymentExpiresAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final List<OrderItem> items = new ArrayList<>();

    protected Order() { }

    public Order(
            Concert concert,
            String customerEmail,
            String accessTokenHash,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        this.concert = concert;
        this.customerEmail = customerEmail;
        this.accessTokenHash = accessTokenHash;
        this.status = OrderStatus.RESERVED;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.paymentExpiresAt = null;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
        recalculateTotalAmount();
    }

    public void markAsPaid() {
        if (this.status == OrderStatus.PAID) return;

        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Only payment-pending orders can be marked as paid.");
        }

        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsPaymentPending(
            LocalDateTime paymentExpiresAt
    ) {
        if (this.status == OrderStatus.PAYMENT_PENDING) return;

        if (this.status != OrderStatus.RESERVED) {
            throw new IllegalStateException("Only reserved orders can be marked as payment pending.");
        }

        this.paymentExpiresAt = paymentExpiresAt;
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void expire() {
        if (
                this.status == OrderStatus.RESERVED ||
                this.status == OrderStatus.PAYMENT_PENDING
        ) {
            this.status = OrderStatus.EXPIRED;
        }
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELLED) return;

        if (this.status != OrderStatus.RESERVED) {
            throw new IllegalStateException("Only reserved orders can be cancelled.");
        }

        this.status = OrderStatus.CANCELLED;
    }

    private void recalculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return this.id;
    }

    public Concert getConcert() {
        return this.concert;
    }

    public String getCustomerEmail() {
        return this.customerEmail;
    }

    public String getAccessTokenHash() {
        return this.accessTokenHash;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public LocalDateTime getPaidAt() {
        return this.paidAt;
    }

    public List<OrderItem> getItems() {
        return this.items;
    }

    public boolean shouldExpire() {
        return this.status == OrderStatus.RESERVED
                && this.expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean shouldExpirePayment() {
        return this.status == OrderStatus.PAYMENT_PENDING
                && this.paymentExpiresAt != null
                && this.paymentExpiresAt.isBefore(LocalDateTime.now());
    }
}
