package accordion_symphonic.ticketing.concert;

import accordion_symphonic.ticketing.concert.dto.ConcertRequest;
import accordion_symphonic.ticketing.concert.exception.ConcertCannotBeArchivedException;
import accordion_symphonic.ticketing.concert.exception.ConcertCannotBeCancelledException;
import accordion_symphonic.ticketing.concert.exception.ConcertCannotBePublishedException;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
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

    public void publish() {
        if (status == ConcertStatus.PUBLISHED) return;

        if (status != ConcertStatus.DRAFT) {
            throw new ConcertCannotBePublishedException(this.id);
        }

        this.status = ConcertStatus.PUBLISHED;
    }

    public void archive() {
        if (status == ConcertStatus.ARCHIVED) return;

        if (status != ConcertStatus.PUBLISHED) {
            throw new ConcertCannotBeArchivedException(this.id);
        }

        this.status = ConcertStatus.ARCHIVED;
    }

    public void cancel() {
        if (status == ConcertStatus.CANCELLED) {
            return;
        }

        if (status != ConcertStatus.PUBLISHED) {
            throw new ConcertCannotBeCancelledException(this.id);
        }

        status = ConcertStatus.CANCELLED;
    }
}
