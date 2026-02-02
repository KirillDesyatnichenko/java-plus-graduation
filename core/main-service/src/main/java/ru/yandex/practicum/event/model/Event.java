package ru.yandex.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Entity
@Table(name = "events", schema = "public")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "annotation", nullable = false)
    private String annotation;
    @Column(name = "description", nullable = false)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;
    @Embedded
    private Location location;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(name = "paid")
    private boolean paid;
    @Column(name = "participant_limit")
    private Integer participantLimit;
    @Column(name = "request_moderation")
    private boolean requestModeration;
    @Column(name = "state", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EventState state;
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<Request> requests;
}
