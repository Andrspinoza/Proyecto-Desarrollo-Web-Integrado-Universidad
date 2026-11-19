package com.utp.quesale.iam.interfaces.assemblers;

import com.utp.quesale.iam.application.dtos.EmailVerificationStatusDto;
import com.utp.quesale.iam.interfaces.resources.EmailVerificationStatusResource;

public final class EmailVerificationResourceAssembler {

    private EmailVerificationResourceAssembler() {
    }

    public static EmailVerificationStatusResource toResourceFromDto(EmailVerificationStatusDto dto) {
        return new EmailVerificationStatusResource(dto.studentId(), dto.verified(), dto.verifiedAt());
    }
}
