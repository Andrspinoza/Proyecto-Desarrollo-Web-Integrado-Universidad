package com.utp.quesale.communities.application;

import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.domain.repositories.CommunityPostRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryCommunityPostRepository implements CommunityPostRepository {

    private final List<CommunityPost> store = new ArrayList<>();
    private long nextId = 1;

    @Override
    public CommunityPost save(CommunityPost post) {
        CommunityPost saved = new CommunityPost(nextId++, post.getCommunityId(), post.getAuthorId(),
                post.getContent(), post.getCreatedAt());
        store.add(saved);
        return saved;
    }

    @Override
    public List<CommunityPost> findAllByCommunityId(Long communityId) {
        return store.stream()
                .filter(p -> p.getCommunityId().equals(communityId))
                .sorted(Comparator.comparing(CommunityPost::getCreatedAt).reversed()
                        .thenComparing(CommunityPost::getId, Comparator.reverseOrder()))
                .toList();
    }
}
