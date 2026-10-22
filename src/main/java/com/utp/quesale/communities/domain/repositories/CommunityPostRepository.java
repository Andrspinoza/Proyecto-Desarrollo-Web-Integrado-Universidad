package com.utp.quesale.communities.domain.repositories;

import com.utp.quesale.communities.domain.entities.CommunityPost;

import java.util.List;

public interface CommunityPostRepository {
    CommunityPost save(CommunityPost post);

    List<CommunityPost> findAllByCommunityId(Long communityId);
}
