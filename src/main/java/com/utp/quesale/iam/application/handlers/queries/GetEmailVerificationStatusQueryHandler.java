package com.utp.quesale.iam.application.handlers.queries;

import com.utp.quesale.iam.application.assemblers.IamDtoAssembler;
import com.utp.quesale.iam.application.dtos.EmailVerificationStatusDto;
import com.utp.quesale.iam.application.queries.GetEmailVerificationStatusQuery;
import com.utp.quesale.iam.domain.repositories.EmailVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetEmailVerificationStatusQueryHandler {

    private final EmailVerificationRepository repository;

    public GetEmailVerificationStatusQueryHandler(EmailVerificationRepository repository) {
        this.repository = repository;
    }

    public EmailVerificationStatusDto handle(GetEmailVerificationStatusQuery query) {
        return repository.findVerifiedByStudentId(query.studentId())
                .map(IamDtoAssembler::toStatusDto)
                .orElseGet(() -> IamDtoAssembler.notVerified(query.studentId()));
    }
}
