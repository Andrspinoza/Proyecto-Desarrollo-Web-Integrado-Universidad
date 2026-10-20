package com.utp.quesale.profiles.infrastructure.persistence.jpa.entities;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_record_id", columnNames = "record_id"),
        @UniqueConstraint(name = "uk_student_email", columnNames = "email")
})
public class StudentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_id", nullable = false, length = 36, updatable = false)
    private String recordId;

    // La relación JPA crea la FK real que necesitan MySQL y el diagrama EER.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_student_university"))
    private UniversityJpaEntity university;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "student", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileJpaEntity profile;

    // Mantiene ambos lados de la relación; el perfil contiene la FK a students.
    public void setProfile(ProfileJpaEntity profile) {
        if (this.profile == profile) return;
        if (this.profile != null) this.profile.setStudent(null);
        this.profile = profile;
        if (profile != null) profile.setStudent(this);
    }
}
