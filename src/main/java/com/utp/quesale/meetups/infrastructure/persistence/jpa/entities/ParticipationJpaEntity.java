package com.utp.quesale.meetups.infrastructure.persistence.jpa.entities;

import com.utp.quesale.meetups.domain.enums.ParticipationStatus;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "participations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_participation_meetup_student", columnNames = {"meetup_id", "student_id"})
})
public class ParticipationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meetup_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_participation_meetup"))
    private MeetupJpaEntity meetup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_participation_student"))
    private StudentJpaEntity student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationStatus status;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;
}