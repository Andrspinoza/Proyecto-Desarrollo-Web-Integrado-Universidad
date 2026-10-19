package com.utp.quesale.communities.infrastructure.persistence.jpa.entities;

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
@Table(name = "community_posts")
public class CommunityPostJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_community"))
    private CommunityJpaEntity community;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_author"))
    private StudentJpaEntity author;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
