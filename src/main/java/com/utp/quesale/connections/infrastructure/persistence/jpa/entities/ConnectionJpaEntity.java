package com.utp.quesale.connections.infrastructure.persistence.jpa.entities;

import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
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
@Table(name = "connections", indexes = {
        @Index(name = "idx_connection_requester", columnList = "requester_id"),
        @Index(name = "idx_connection_recipient", columnList = "recipient_id"),
        @Index(name = "idx_connection_status", columnList = "status")
})
public class ConnectionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_connection_requester"))
    private StudentJpaEntity requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_connection_recipient"))
    private StudentJpaEntity recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;
}
