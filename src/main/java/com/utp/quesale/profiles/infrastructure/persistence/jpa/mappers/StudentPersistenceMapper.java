package com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.entities.Profile;
import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.domain.valueobjects.StudentRecordId;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.ProfileJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.InterestJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.SkillJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class StudentPersistenceMapper {
    public Student toDomain(StudentJpaEntity entity) {
        return new Student(
                entity.getId(),
                new StudentRecordId(entity.getRecordId()),
                entity.getUniversity().getId(),
                entity.getEmail(),
                new FullName(entity.getFirstName(), entity.getLastName()),
                entity.getCreatedAt(),
                toProfile(entity.getProfile())
        );
    }

    public StudentJpaEntity toEntity(Student student, UniversityJpaEntity university,
                                     Collection<InterestJpaEntity> interests,
                                     Collection<SkillJpaEntity> skills) {
        return updateEntity(student, new StudentJpaEntity(), university, interests, skills);
    }

    public StudentJpaEntity updateEntity(Student student, StudentJpaEntity entity,
                                         UniversityJpaEntity university,
                                         Collection<InterestJpaEntity> interests,
                                         Collection<SkillJpaEntity> skills) {
        entity.setId(student.getId());
        entity.setRecordId(student.getRecordId().studentRecordId());
        entity.setUniversity(university);
        entity.setEmail(student.getEmail());
        entity.setFirstName(student.getFullName().firstName());
        entity.setLastName(student.getFullName().lastName());
        entity.setCreatedAt(student.getCreatedAt());
        if (student.getProfile() == null) {
            entity.setProfile(null);
        } else {
            Profile source = student.getProfile();
            // Actualiza la entidad existente para conservar su ID y no duplicar el perfil.
            ProfileJpaEntity profile = entity.getProfile();
            if (profile == null) {
                profile = new ProfileJpaEntity();
                entity.setProfile(profile);
            }
            profile.setCareer(source.getCareer().value());
            profile.setCycle(source.getCycle().value());
            profile.setDescription(source.getDescription());
            profile.setVisibility(source.getVisibility());
            profile.getInterests().clear();
            profile.getInterests().addAll(interests);
            profile.getSkills().clear();
            profile.getSkills().addAll(skills);
        }
        return entity;
    }

    private Profile toProfile(ProfileJpaEntity entity) {
        if (entity == null) return null;
        return new Profile(new Career(entity.getCareer()), new StudyCycle(entity.getCycle()),
                entity.getDescription(), entity.getVisibility(),
                entity.getInterests().stream().map(InterestJpaEntity::getId).toList(),
                entity.getSkills().stream().map(SkillJpaEntity::getId).toList());
    }
}
