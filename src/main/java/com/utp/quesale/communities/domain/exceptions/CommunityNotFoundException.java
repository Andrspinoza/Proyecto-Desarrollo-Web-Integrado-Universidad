package com.utp.quesale.communities.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class CommunityNotFoundException extends ResourceNotFoundException {
    public CommunityNotFoundException(Long communityId) {
        super(String.format("Community with ID %s not found", communityId));
    }
}
