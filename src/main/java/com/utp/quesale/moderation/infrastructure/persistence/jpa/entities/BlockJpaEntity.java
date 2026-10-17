package com.utp.quesale.moderation.infrastructure.persistence.jpa.entities;

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
@Table(name = "blocks", uniqueConstraints = {
        @UniqueConstraint(name = "uk_blocker_blocked", columnNames = {"blocker_id", "blocked_id"})
})
public class BlockJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocker_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_block_blocker"))
    private StudentJpaEntity blocker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocked_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_block_blocked"))
    private StudentJpaEntity blocked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
