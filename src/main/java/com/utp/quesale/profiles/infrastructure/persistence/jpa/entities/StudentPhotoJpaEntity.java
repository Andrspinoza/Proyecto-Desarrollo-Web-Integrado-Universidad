package com.utp.quesale.profiles.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "student_photos", uniqueConstraints =
        @UniqueConstraint(name = "uk_student_photo_key", columnNames = "photo_key"))
public class StudentPhotoJpaEntity {
    @Id
    @Column(name = "student_id")
    private Long studentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "student_id", foreignKey = @ForeignKey(name = "fk_student_photo_student"))
    private StudentJpaEntity student;

    @Column(name = "photo_key", nullable = false, length = 36)
    private String photoKey;

    @Column(name = "content_type", nullable = false, length = 20)
    private String contentType;

    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.LONGVARBINARY)
    @Column(nullable = false, length = 1_048_576)
    private byte[] data;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
