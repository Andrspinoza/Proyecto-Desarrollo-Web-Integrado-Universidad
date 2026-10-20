package com.utp.quesale.communities.infrastructure.persistence.jpa.entities;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "communities", uniqueConstraints = {
        @UniqueConstraint(name = "uk_community_university_name", columnNames = {"university_id", "name"})
})
public class CommunityJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_community_university"))
    private UniversityJpaEntity university;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_community_creator"))
    private StudentJpaEntity creator;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MembershipJpaEntity> memberships = new ArrayList<>();

    public void replaceMemberships(Collection<MembershipJpaEntity> memberships) {
        Objects.requireNonNull(memberships, "Community memberships are required");
        List<MembershipJpaEntity> replacements = new ArrayList<>(memberships);
        this.memberships.clear();
        for (MembershipJpaEntity membership : replacements) {
            membership.setCommunity(this);
            this.memberships.add(membership);
        }
    }
}
