package com.utp.quesale.profiles.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.repositories.InterestRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers.InterestPersistenceMapper;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories.InterestJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InterestRepositoryAdapter implements InterestRepository {
    private final InterestJpaRepository repository;
    private final InterestPersistenceMapper mapper;

    public InterestRepositoryAdapter(InterestJpaRepository repository, InterestPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Interest save(Interest interest) {
        return mapper.toDomain(repository.save(mapper.toEntity(interest)));
    }

    @Override
    public Optional<Interest> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Interest> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }
}
