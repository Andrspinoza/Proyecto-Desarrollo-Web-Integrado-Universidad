package com.utp.quesale.iam.application;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Repositorio en memoria para probar los handlers sin base de datos
class InMemoryUniversityRepository implements UniversityRepository {

    private final Map<Long, University> store = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public University save(University university) {
        University saved = new University(nextId++, university.getName(), university.getEmailDomain(),
                university.isActive(), university.getCreatedAt());
        store.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public University update(University university) {
        store.put(university.getId(), university);
        return university;
    }

    @Override
    public Optional<University> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<University> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public boolean existsByEmailDomain(String emailDomain) {
        return store.values().stream().anyMatch(u -> u.getEmailDomain().value().equalsIgnoreCase(emailDomain));
    }
}
