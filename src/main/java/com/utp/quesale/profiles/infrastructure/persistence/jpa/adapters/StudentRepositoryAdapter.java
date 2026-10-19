package com.utp.quesale.profiles.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.iam.infrastructure.persistence.jpa.repositories.UniversityJpaRepository;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers.StudentPersistenceMapper;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories.StudentJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories.InterestJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories.SkillJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.InterestJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.SkillJpaEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class StudentRepositoryAdapter implements StudentRepository {
    private final StudentJpaRepository repository;
    private final StudentPersistenceMapper mapper;
    private final UniversityJpaRepository universityRepository;
    private final InterestJpaRepository interestRepository;
    private final SkillJpaRepository skillRepository;

    public StudentRepositoryAdapter(StudentJpaRepository repository, StudentPersistenceMapper mapper,
                                    UniversityJpaRepository universityRepository,
                                    InterestJpaRepository interestRepository, SkillJpaRepository skillRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.universityRepository = universityRepository;
        this.interestRepository = interestRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    @Transactional
    public Student save(Student student) {
        UniversityJpaEntity university = universityRepository.findById(student.getUniversityId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "University not found: " + student.getUniversityId()));
        List<InterestJpaEntity> interests = student.getProfile() == null ? List.of()
                : student.getProfile().getInterestIds().stream().map(id -> interestRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Interest not found: " + id))).toList();
        List<SkillJpaEntity> skills = student.getProfile() == null ? List.of()
                : student.getProfile().getSkillIds().stream().map(id -> skillRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + id))).toList();
        StudentJpaEntity entity = student.getId() == null
                ? mapper.toEntity(student, university, interests, skills)
                : mapper.updateEntity(student, repository.findById(student.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Student not found: " + student.getId())),
                        university, interests, skills);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public Student update(Student student) {
        return save(student);
    }

    @Override
    public Optional<Student> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Student> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }
}
