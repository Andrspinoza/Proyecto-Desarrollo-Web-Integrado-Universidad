package com.utp.quesale.communities.application;

import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.repositories.CommunityRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Repositorio en memoria para probar los handlers sin base de datos
public class InMemoryCommunityRepository implements CommunityRepository {

    private final Map<Long, Community> store = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public Community save(Community community) {
        Community saved = new Community(nextId++, community.getUniversityId(), community.getName(),
                community.getDescription(), community.getCreatorId(), community.getCreatedAt(),
                community.getMemberships());
        store.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public Community update(Community community) {
        store.put(community.getId(), copyOf(community));
        return community;
    }

    // Como la base real, devuelve una copia: los cambios sin guardar no se ven en el repositorio
    @Override
    public Optional<Community> findById(Long id) {
        return Optional.ofNullable(store.get(id)).map(InMemoryCommunityRepository::copyOf);
    }

    private static Community copyOf(Community c) {
        return new Community(c.getId(), c.getUniversityId(), c.getName(), c.getDescription(),
                c.getCreatorId(), c.getCreatedAt(), c.getMemberships());
    }

    @Override
    public List<Community> findAllByUniversityId(Long universityId) {
        return new ArrayList<>(store.values().stream()
                .filter(c -> c.getUniversityId().equals(universityId)).toList());
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public boolean existsByUniversityIdAndName(Long universityId, String name) {
        return store.values().stream().anyMatch(c ->
                c.getUniversityId().equals(universityId) && c.getName().equalsIgnoreCase(name));
    }
}
