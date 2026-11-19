package com.utp.quesale.iam.application.assemblers;

import com.utp.quesale.iam.application.dtos.EmailVerificationStatusDto;
import com.utp.quesale.iam.application.dtos.UniversityDto;
import com.utp.quesale.iam.domain.aggregates.EmailVerification;
import com.utp.quesale.iam.domain.aggregates.University;

public final class IamDtoAssembler {

    private IamDtoAssembler() {
    }

    public static UniversityDto toDto(University university) {
        return new UniversityDto(
                university.getId(),
                university.getName(),
                university.getEmailDomain().value(),
                university.isActive(),
                university.getCreatedAt()
        );
    }

    public static EmailVerificationStatusDto toStatusDto(EmailVerification verification) {
        return new EmailVerificationStatusDto(
                verification.getStudentId(),
                verification.isVerified(),
                verification.getVerifiedAt()
        );
    }

    public static EmailVerificationStatusDto notVerified(Long studentId) {
        return new EmailVerificationStatusDto(studentId, false, null);
    }
}
