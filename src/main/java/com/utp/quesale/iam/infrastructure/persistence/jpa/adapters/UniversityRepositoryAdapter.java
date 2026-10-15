package com.utp.quesale.iam.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.infrastructure.persistence.jpa.mappers.UniversityPersistenceMapper;
import com.utp.quesale.iam.infrastructure.persistence.jpa.repositories.UniversityJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UniversityRepositoryAdapter implements UniversityRepository {
    private final UniversityJpaRepository repository;
    private final UniversityPersistenceMapper mapper;

    public UniversityRepositoryAdapter(UniversityJpaRepository repository, UniversityPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public University save(University university) {
        return mapper.toDomain(repository.save(mapper.toEntity(university)));
    }

    @Override
    public University update(University university) {
        return save(university);
    }

    @Override
    public Optional<University> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<University> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByEmailDomain(String emailDomain) {
        return repository.existsByEmailDomainIgnoreCase(emailDomain);
    }
}
