package com.utp.quesale.meetups.infrastructure.persistence.jpa.entities;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.meetups.domain.enums.MeetupStatus;
import com.utp.quesale.meetups.domain.enums.MeetupType;
import com.utp.quesale.meetups.domain.enums.MeetupVisibility;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "meetups")
public class MeetupJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_meetup_organizer"))
    private StudentJpaEntity organizer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_meetup_university"))
    private UniversityJpaEntity university;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "meetup_type", nullable = false, length = 20)
    private MeetupType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MeetupVisibility visibility;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "place_name", nullable = false, length = 120)
    private String placeName;

    @Column(name = "place_address", nullable = false, length = 200)
    private String placeAddress;

    @Column(name = "place_district", nullable = false, length = 60)
    private String placeDistrict;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MeetupStatus status;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "meetup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipationJpaEntity> participations = new ArrayList<>();

    public void replaceParticipations(Collection<ParticipationJpaEntity> participations) {
        Objects.requireNonNull(participations, "Meetup participations are required");
        List<ParticipationJpaEntity> replacements = new ArrayList<>(participations);
        this.participations.clear();
        for (ParticipationJpaEntity participation : replacements) {
            participation.setMeetup(this);
            this.participations.add(participation);
        }
    }
}