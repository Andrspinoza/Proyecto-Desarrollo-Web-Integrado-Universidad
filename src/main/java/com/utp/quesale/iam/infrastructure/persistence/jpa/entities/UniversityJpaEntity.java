package com.utp.quesale.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "universities", uniqueConstraints = {
        @UniqueConstraint(name = "uk_university_name", columnNames = "name"),
        @UniqueConstraint(name = "uk_university_email_domain", columnNames = "email_domain")
})
public class UniversityJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "email_domain", nullable = false, length = 80)
    private String emailDomain;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
