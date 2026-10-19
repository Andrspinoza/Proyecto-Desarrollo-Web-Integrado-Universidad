package com.utp.quesale.profiles.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.profiles.domain.aggregates.Skill;
import com.utp.quesale.profiles.domain.repositories.SkillRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers.SkillPersistenceMapper;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories.SkillJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SkillRepositoryAdapter implements SkillRepository {
    private final SkillJpaRepository repository;
    private final SkillPersistenceMapper mapper;

    public SkillRepositoryAdapter(SkillJpaRepository repository, SkillPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Skill save(Skill skill) {
        return mapper.toDomain(repository.save(mapper.toEntity(skill)));
    }

    @Override
    public Optional<Skill> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Skill> findAll() {
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
