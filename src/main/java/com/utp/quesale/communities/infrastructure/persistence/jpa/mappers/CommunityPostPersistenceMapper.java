package com.utp.quesale.communities.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.infrastructure.persistence.jpa.entities.CommunityJpaEntity;
import com.utp.quesale.communities.infrastructure.persistence.jpa.entities.CommunityPostJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CommunityPostPersistenceMapper {

    public CommunityPost toDomain(CommunityPostJpaEntity entity) {
        return new CommunityPost(
                entity.getId(),
                entity.getCommunity().getId(),
                entity.getAuthor().getId(),
                entity.getContent(),
                entity.getCreatedAt());
    }

    public CommunityPostJpaEntity toEntity(CommunityPost post, CommunityJpaEntity community, StudentJpaEntity author) {
        CommunityPostJpaEntity entity = new CommunityPostJpaEntity();
        entity.setId(post.getId());
        entity.setCommunity(community);
        entity.setAuthor(author);
        entity.setContent(post.getContent());
        entity.setCreatedAt(post.getCreatedAt());
        return entity;
    }
}
