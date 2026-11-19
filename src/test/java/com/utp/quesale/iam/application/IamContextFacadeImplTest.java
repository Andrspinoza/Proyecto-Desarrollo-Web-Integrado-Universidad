package com.utp.quesale.iam.application;

import com.utp.quesale.iam.application.acl.IamContextFacadeImpl;
import com.utp.quesale.iam.domain.aggregates.EmailVerification;
import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IamContextFacadeImplTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");

    private final InMemoryUniversityRepository universities = new InMemoryUniversityRepository();
    private final InMemoryEmailVerificationRepository verifications = new InMemoryEmailVerificationRepository();
    private final IamContextFacadeImpl facade = new IamContextFacadeImpl(universities, verifications, false);
    private Long utpId;

    @BeforeEach
    void setUp() {
        utpId = universities.save(new University("UTP", new EmailDomain("utp.edu.pe"))).getId();
    }

    @Test
    void activeUniversityExists() {
        assertTrue(facade.isActiveUniversity(utpId));
    }

    @Test
    void unknownUniversityIsNotActive() {
        assertFalse(facade.isActiveUniversity(99L));
    }

    @Test
    void deactivatedUniversityIsNotActive() {
        University university = universities.findById(utpId).orElseThrow();
        university.deactivate();
        universities.update(university);

        assertFalse(facade.isActiveUniversity(utpId));
    }

    @Test
    void invalidIdsAreNotActive() {
        assertFalse(facade.isActiveUniversity(null));
        assertFalse(facade.isActiveUniversity(0L));
    }

    @Test
    void acceptsAnEmailOfTheInstitutionalDomain() {
        assertTrue(facade.universityAcceptsEmail(utpId, "camila@utp.edu.pe"));
        assertTrue(facade.universityAcceptsEmail(utpId, "CAMILA@UTP.EDU.PE"));
    }

    @Test
    void rejectsAnEmailOfAnotherDomain() {
        assertFalse(facade.universityAcceptsEmail(utpId, "camila@gmail.com"));
        assertFalse(facade.universityAcceptsEmail(utpId, "camila@upc.edu.pe"));
        assertFalse(facade.universityAcceptsEmail(utpId, "sin-arroba"));
        assertFalse(facade.universityAcceptsEmail(utpId, null));
    }

    @Test
    void rejectsAnEmailWhenTheUniversityDoesNotExist() {
        assertFalse(facade.universityAcceptsEmail(99L, "camila@utp.edu.pe"));
        assertFalse(facade.universityAcceptsEmail(null, "camila@utp.edu.pe"));
    }

    @Test
    void studentWithoutVerificationIsNotVerified() {
        assertFalse(facade.isEmailVerified(7L));
        assertFalse(facade.isEmailVerified(null));
        assertFalse(facade.isEmailVerified(0L));
    }

    @Test
    void studentWithPendingVerificationIsNotVerified() {
        verifications.save(EmailVerification.issue(7L, "camila@utp.edu.pe", "hash", Duration.ofHours(1), NOW));

        assertFalse(facade.isEmailVerified(7L));
    }

    @Test
    void studentWithConfirmedVerificationIsVerified() {
        EmailVerification verification = EmailVerification.issue(7L, "camila@utp.edu.pe", "hash", Duration.ofHours(1), NOW);
        verification.confirm(NOW.plusSeconds(5));
        verifications.save(verification);

        assertTrue(facade.isEmailVerified(7L));
        assertFalse(facade.isEmailVerified(8L));
    }

    @Test
    void reportsWhetherVerificationIsRequired() {
        assertFalse(facade.isEmailVerificationRequired());
        assertTrue(new IamContextFacadeImpl(universities, verifications, true).isEmailVerificationRequired());
    }
}
