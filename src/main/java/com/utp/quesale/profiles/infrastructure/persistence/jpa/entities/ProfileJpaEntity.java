package com.utp.quesale.profiles.infrastructure.persistence.jpa.entities;

import com.utp.quesale.profiles.domain.valueobjects.Visibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "profiles", uniqueConstraints =
        @UniqueConstraint(name = "uk_profile_student", columnNames = "student_id"))
public class ProfileJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_profile_student"))
    private StudentJpaEntity student;

    @Column(nullable = false, length = 80)
    private String career;

    @Column(nullable = false)
    private int cycle;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Visibility visibility;

    // Los catálogos se comparten: guardar o borrar un perfil no los modifica.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "profile_interests",
            joinColumns = @JoinColumn(name = "profile_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "interest_id", nullable = false),
            foreignKey = @ForeignKey(name = "fk_profile_interest_profile"),
            inverseForeignKey = @ForeignKey(name = "fk_profile_interest_interest"),
            uniqueConstraints = @UniqueConstraint(name = "uk_profile_interest",
                    columnNames = {"profile_id", "interest_id"}))
    private Set<InterestJpaEntity> interests = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "profile_skills",
            joinColumns = @JoinColumn(name = "profile_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "skill_id", nullable = false),
            foreignKey = @ForeignKey(name = "fk_profile_skill_profile"),
            inverseForeignKey = @ForeignKey(name = "fk_profile_skill_skill"),
            uniqueConstraints = @UniqueConstraint(name = "uk_profile_skill",
                    columnNames = {"profile_id", "skill_id"}))
    private Set<SkillJpaEntity> skills = new LinkedHashSet<>();
}
