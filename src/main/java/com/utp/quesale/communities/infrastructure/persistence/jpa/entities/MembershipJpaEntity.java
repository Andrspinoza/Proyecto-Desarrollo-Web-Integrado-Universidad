package com.utp.quesale.communities.infrastructure.persistence.jpa.entities;

import com.utp.quesale.communities.domain.valueobjects.MemberRole;
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
@Table(name = "memberships", uniqueConstraints = {
        @UniqueConstraint(name = "uk_membership_community_student", columnNames = {"community_id", "student_id"})
})
public class MembershipJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_membership_community"))
    private CommunityJpaEntity community;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_membership_student"))
    private StudentJpaEntity student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;
}
