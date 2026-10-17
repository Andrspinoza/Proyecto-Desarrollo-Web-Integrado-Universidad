package com.utp.quesale.profiles.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.iam.infrastructure.persistence.jpa.repositories.UniversityJpaRepository;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers.StudentPersistenceMapper;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories.StudentJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepositoryAdapter implements StudentRepository {
    private final StudentJpaRepository repository;
    private final StudentPersistenceMapper mapper;
    private final UniversityJpaRepository universityRepository;

    public StudentRepositoryAdapter(StudentJpaRepository repository, StudentPersistenceMapper mapper,
                                    UniversityJpaRepository universityRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.universityRepository = universityRepository;
    }

    @Override
    public Student save(Student student) {
        UniversityJpaEntity university = universityRepository.findById(student.getUniversityId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "University not found: " + student.getUniversityId()));
        return mapper.toDomain(repository.save(mapper.toEntity(student, university)));
    }

    @Override
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
