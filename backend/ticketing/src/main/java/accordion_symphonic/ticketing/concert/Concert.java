package accordion_symphonic.ticketing.concert;

import accordion_symphonic.ticketing.concert.dto.ConcertRequest;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private LocalDateTime startTime;

    private String location;

    @Enumerated(EnumType.STRING)
    private ConcertStatus status;

    protected Concert() {}

    public Concert(String title, String description, LocalDateTime startTime, String location) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.location = location;
        this.status = ConcertStatus.DRAFT;
    }

    public void updateConcert(ConcertRequest update) {
        this.title = update.title();
        this.description = update.description();
        this.startTime = update.startTime();
        this.location = update.location();
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public String getLocation() {
        return this.location;
    }

    public ConcertStatus getStatus() {
        return this.status;
    }

    public void publish() {
        this.status = ConcertStatus.PUBLISHED;
    }

    public void archive() {
        this.status = ConcertStatus.ARCHIVED;
    }
}
