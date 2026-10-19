package com.utp.quesale.moderation.infrastructure.persistence.jpa.entities;

import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;
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
@Table(name = "reports")
public class ReportJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_report_reporter"))
    private StudentJpaEntity reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportReason reason;

    @Column(length = 500)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "reviewed_by_account_id")
    private Long reviewedByAccountId;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(length = 500)
    private String resolution;
}
