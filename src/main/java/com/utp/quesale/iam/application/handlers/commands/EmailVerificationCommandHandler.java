package com.utp.quesale.iam.application.handlers.commands;

import com.utp.quesale.iam.application.assemblers.IamDtoAssembler;
import com.utp.quesale.iam.application.commands.ConfirmEmailVerificationCommand;
import com.utp.quesale.iam.application.commands.IssueEmailVerificationCommand;
import com.utp.quesale.iam.application.commands.ResendEmailVerificationCommand;
import com.utp.quesale.iam.application.dtos.EmailVerificationStatusDto;
import com.utp.quesale.iam.domain.aggregates.EmailVerification;
import com.utp.quesale.iam.domain.exceptions.EmailAlreadyVerifiedException;
import com.utp.quesale.iam.domain.exceptions.EmailVerificationNotFoundException;
import com.utp.quesale.iam.domain.exceptions.InvalidVerificationTokenException;
import com.utp.quesale.iam.domain.exceptions.ResendTooSoonException;
import com.utp.quesale.iam.domain.repositories.EmailVerificationRepository;
import com.utp.quesale.iam.domain.services.VerificationEmailSender;
import com.utp.quesale.iam.domain.valueobjects.VerificationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class EmailVerificationCommandHandler {

    static final String CONFIRM_PATH = "/api/v1/email-verifications/confirm?token=";

    private final EmailVerificationRepository repository;
    private final VerificationEmailSender sender;
    private final Clock clock;
    private final String baseUrl;
    private final Duration ttl;
    private final Duration resendCooldown;

    public EmailVerificationCommandHandler(EmailVerificationRepository repository,
                                           VerificationEmailSender sender,
                                           Clock clock,
                                           @Value("${app.verification.base-url:http://localhost:8080}") String baseUrl,
                                           @Value("${app.verification.ttl-hours:24}") long ttlHours,
                                           @Value("${app.verification.resend-cooldown-seconds:60}") long cooldownSeconds) {
        this.repository = repository;
        this.sender = sender;
        this.clock = clock;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.ttl = Duration.ofHours(ttlHours);
        this.resendCooldown = Duration.ofSeconds(cooldownSeconds);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(IssueEmailVerificationCommand command) {
        issue(command.studentId(), command.email(), command.recipientName());
    }

    @Transactional
    public EmailVerificationStatusDto handle(ConfirmEmailVerificationCommand command) {
        EmailVerification verification = repository
                .findByTokenHash(VerificationToken.hashOf(command.token()))
                .orElseThrow(InvalidVerificationTokenException::new);
        verification.confirm(clock.instant());
        return IamDtoAssembler.toStatusDto(repository.save(verification));
    }

    @Transactional
    public void handle(ResendEmailVerificationCommand command) {
        Long studentId = command.studentId();
        if (repository.findVerifiedByStudentId(studentId).isPresent())
            throw new EmailAlreadyVerifiedException(studentId);
        EmailVerification last = repository.findLatestByStudentId(studentId)
                .orElseThrow(() -> new EmailVerificationNotFoundException(studentId));
        Instant now = clock.instant();
        Duration elapsed = Duration.between(last.getCreatedAt(), now);
        if (elapsed.compareTo(resendCooldown) < 0)
            throw new ResendTooSoonException(resendCooldown.minus(elapsed).toSeconds() + 1);
        issue(studentId, last.getEmail(), localPart(last.getEmail()));
    }

    private void issue(Long studentId, String email, String recipientName) {
        VerificationToken token = VerificationToken.generate();
        EmailVerification verification = EmailVerification.issue(studentId, email, token.hash(), ttl, clock.instant());
        repository.save(verification);
        sender.send(verification.getEmail(), recipientName, baseUrl + CONFIRM_PATH + token.raw());
    }

    private static String localPart(String email) {
        return email.substring(0, email.indexOf('@'));
    }
}
