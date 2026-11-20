package com.utp.quesale.iam.infrastructure.security;

import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.iam.application.commands.ResendEmailVerificationCommand;
import com.utp.quesale.iam.application.commands.IssueEmailVerificationCommand;
import com.utp.quesale.iam.application.handlers.commands.EmailVerificationCommandHandler;
import com.utp.quesale.iam.domain.repositories.EmailVerificationRepository;
import com.utp.quesale.profiles.application.commands.CreateStudentCommand;
import com.utp.quesale.profiles.application.handlers.commands.CreateStudentCommandHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AccountJpaRepository accounts;
    private final CreateStudentCommandHandler students;
    private final IamContextFacade iam;
    private final JwtService jwt;
    private final EmailVerificationCommandHandler verification;
    private final EmailVerificationRepository verificationRepository;
    private final AuthRateLimiter limiter;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    private final String dummyHash = passwords.encode("dummy-password-not-a-valid-account");
    public AuthController(AccountJpaRepository accounts, CreateStudentCommandHandler students,
                          IamContextFacade iam, JwtService jwt, EmailVerificationCommandHandler verification,
                          EmailVerificationRepository verificationRepository, AuthRateLimiter limiter) {
        this.accounts = accounts; this.students = students; this.iam = iam; this.jwt = jwt; this.verification = verification;
        this.verificationRepository = verificationRepository; this.limiter = limiter;
    }
    public record Registration(@NotNull @Positive Long universityId, @NotBlank @Email @Size(max=120) String email,
                               @NotBlank @Size(min=8,max=72) String password,
                               @NotBlank @Size(max=60) String firstName, @NotBlank @Size(max=60) String lastName) {}
    public record Login(@NotBlank @Email @Size(max=120) String email, @NotBlank @Size(max=72) String password) {}
    public record RegistrationResponse(Long studentId, String message) {}
    public record LoginResponse(String accessToken, String tokenType, int expiresIn, Long studentId) {}

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public RegistrationResponse register(@Valid @RequestBody Registration body) {
        String email = body.email().trim().toLowerCase(Locale.ROOT);
        if (accounts.findByEmailIgnoreCase(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede registrar esta cuenta");
        var student = students.handle(new CreateStudentCommand(body.universityId(), email, body.firstName(), body.lastName()));
        accounts.save(new AccountEntity(email, passwords.encode(body.password()), student.id()));
        return new RegistrationResponse(student.id(), "Revisa tu correo institucional para verificar tu cuenta");
    }
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody Login body) {
        String email = body.email().trim();
        if (limiter.emailBlocked(email))
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos; espera unos minutos");
        var account = accounts.findByEmailIgnoreCase(email).orElse(null);
        boolean valid = passwords.matches(body.password(), account == null ? dummyHash : account.getPasswordHash());
        if (account == null || !valid) {
            limiter.recordFailure(email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }
        limiter.clearFailures(email);
        if (!iam.isEmailVerified(account.getStudentId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verifica tu correo institucional primero");
        return new LoginResponse(jwt.issue(account.getStudentId()), "Bearer", 3600, account.getStudentId());
    }
    @PostMapping("/resend-verification") @ResponseStatus(HttpStatus.ACCEPTED)
    public void resend(@Valid @RequestBody Login body) {
        String email = body.email().trim();
        if (limiter.emailBlocked(email))
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos; espera unos minutos");
        var account = accounts.findByEmailIgnoreCase(email).orElse(null);
        boolean valid = passwords.matches(body.password(), account == null ? dummyHash : account.getPasswordHash());
        if (account == null || !valid) limiter.recordFailure(email);
        if (account != null && valid && !iam.isEmailVerified(account.getStudentId())) {
            if (verificationRepository.findLatestByStudentId(account.getStudentId()).isPresent())
                verification.handle(new ResendEmailVerificationCommand(account.getStudentId()));
            else
                verification.handle(new IssueEmailVerificationCommand(account.getStudentId(), account.getEmail(), account.getEmail().split("@", 2)[0]));
        }
    }
}
