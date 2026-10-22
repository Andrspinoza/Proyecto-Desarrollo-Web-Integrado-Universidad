package com.utp.quesale.communities.interfaces.assemblers;

import com.utp.quesale.communities.application.dtos.MembershipDto;
import com.utp.quesale.communities.domain.entities.Membership;
import com.utp.quesale.communities.interfaces.resources.MembershipResource;

public class MembershipResourceAssembler {

    public static MembershipResource toResourceFromDto(MembershipDto dto) {
        return new MembershipResource(dto.studentId(), dto.role(), dto.joinedAt());
    }

    public static MembershipResource toResourceFromEntity(Membership membership) {
        return new MembershipResource(
                membership.getStudentId(),
                membership.getRole().name(),
                membership.getJoinedAt()
        );
    }
}
