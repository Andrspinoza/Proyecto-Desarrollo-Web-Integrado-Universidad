package com.utp.quesale.communities.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.entities.Membership;
import com.utp.quesale.communities.infrastructure.persistence.jpa.entities.CommunityJpaEntity;
import com.utp.quesale.communities.infrastructure.persistence.jpa.entities.MembershipJpaEntity;
import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class CommunityPersistenceMapper {

    public Community toDomain(CommunityJpaEntity entity) {
        List<Membership> memberships = entity.getMemberships().stream()
                .map(membership -> new Membership(
                        membership.getId(),
                        membership.getStudent().getId(),
                        membership.getRole(),
                        membership.getJoinedAt()))
                .toList();

        return new Community(
                entity.getId(),
                entity.getUniversity().getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreator().getId(),
                entity.getCreatedAt(),
                memberships);
    }

    public CommunityJpaEntity toEntity(
            Community community,
            UniversityJpaEntity university,
            StudentJpaEntity creator,
            Function<Long, StudentJpaEntity> studentReference) {
        CommunityJpaEntity entity = new CommunityJpaEntity();
        entity.setId(community.getId());
        entity.setUniversity(university);
        entity.setName(community.getName());
        entity.setDescription(community.getDescription());
        entity.setCreator(creator);
        entity.setCreatedAt(community.getCreatedAt());

        List<MembershipJpaEntity> memberships = community.getMemberships().stream()
                .map(membership -> toMembershipEntity(membership, studentReference))
                .toList();
        entity.replaceMemberships(memberships);
        return entity;
    }

    private MembershipJpaEntity toMembershipEntity(
            Membership membership,
            Function<Long, StudentJpaEntity> studentReference) {
        MembershipJpaEntity entity = new MembershipJpaEntity();
        entity.setId(membership.getId());
        entity.setStudent(studentReference.apply(membership.getStudentId()));
        entity.setRole(membership.getRole());
        entity.setJoinedAt(membership.getJoinedAt());
        return entity;
    }
}
