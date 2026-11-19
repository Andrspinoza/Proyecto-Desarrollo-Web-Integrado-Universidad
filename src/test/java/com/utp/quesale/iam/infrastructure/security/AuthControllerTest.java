package com.utp.quesale.iam.infrastructure.security;

import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.iam.application.handlers.commands.EmailVerificationCommandHandler;
import com.utp.quesale.iam.domain.repositories.EmailVerificationRepository;
import com.utp.quesale.iam.application.commands.IssueEmailVerificationCommand;
import com.utp.quesale.profiles.application.handlers.commands.CreateStudentCommandHandler;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Test void resendCanRecoverFromFailedInitialEmail() {
        AccountJpaRepository accounts = mock(AccountJpaRepository.class);
        IamContextFacade iam = mock(IamContextFacade.class);
        EmailVerificationCommandHandler verification = mock(EmailVerificationCommandHandler.class);
        EmailVerificationRepository records = mock(EmailVerificationRepository.class);
        var account = new AccountEntity("a@uni.edu", new BCryptPasswordEncoder().encode("Password123"), 3L);
        when(accounts.findByEmailIgnoreCase("a@uni.edu")).thenReturn(Optional.of(account));
        when(records.findLatestByStudentId(3L)).thenReturn(Optional.empty());
        var auth = new AuthController(accounts, mock(CreateStudentCommandHandler.class), iam,
                new JwtService("a-secret-with-at-least-32-bytes-long"), verification, records);
        auth.resend(new AuthController.Login("a@uni.edu", "Password123"));
        verify(verification).handle(new IssueEmailVerificationCommand(3L, "a@uni.edu", "a"));
    }
    @Test void loginRequiresVerificationAndDoesNotAcceptWrongPassword() {
        AccountJpaRepository accounts = mock(AccountJpaRepository.class);
        IamContextFacade iam = mock(IamContextFacade.class);
        JwtService jwt = new JwtService("a-secret-with-at-least-32-bytes-long");
        var account = new AccountEntity("a@uni.edu", new BCryptPasswordEncoder().encode("Password123"), 3L);
        when(accounts.findByEmailIgnoreCase("a@uni.edu")).thenReturn(Optional.of(account));
        var auth = new AuthController(accounts, mock(CreateStudentCommandHandler.class), iam, jwt, mock(EmailVerificationCommandHandler.class), mock(EmailVerificationRepository.class));
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> auth.login(new AuthController.Login("a@uni.edu", "wrong"))).getStatusCode().value());
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> auth.login(new AuthController.Login("a@uni.edu", "Password123"))).getStatusCode().value());
        when(iam.isEmailVerified(3L)).thenReturn(true);
        var result = auth.login(new AuthController.Login("a@uni.edu", "Password123"));
        assertEquals(3L, jwt.verify(result.accessToken()));
    }
}
