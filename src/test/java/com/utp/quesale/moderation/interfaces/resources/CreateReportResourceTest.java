package com.utp.quesale.moderation.interfaces.resources;

import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateReportResourceTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void requiresTargetTypeTargetIdAndReason() {
        CreateReportResource resource = new CreateReportResource(null, null, null, null);

        assertEquals(3, VALIDATOR.validate(resource).size());
    }

    @Test
    void requiresPositiveTargetIdAndLimitsDetailLength() {
        CreateReportResource invalidId = new CreateReportResource(
                ReportTargetType.STUDENT, 0L, ReportReason.OTHER, null);
        CreateReportResource longDetail = new CreateReportResource(
                ReportTargetType.STUDENT, 1L, ReportReason.OTHER, "x".repeat(501));

        assertFalse(VALIDATOR.validate(invalidId).isEmpty());
        assertFalse(VALIDATOR.validate(longDetail).isEmpty());
    }

    @Test
    void acceptsAValidReportResource() {
        CreateReportResource resource = new CreateReportResource(
                ReportTargetType.STUDENT, 1L, ReportReason.HARASSMENT, "Repeated messages");

        assertEquals(0, VALIDATOR.validate(resource).size());
    }
}
