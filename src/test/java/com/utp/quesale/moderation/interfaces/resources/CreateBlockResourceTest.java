package com.utp.quesale.moderation.interfaces.resources;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateBlockResourceTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void requiresPositiveBlockedStudentId() {
        assertEquals(1, VALIDATOR.validate(new CreateBlockResource(null)).size());
        assertFalse(VALIDATOR.validate(new CreateBlockResource(0L)).isEmpty());
    }

    @Test
    void acceptsPositiveBlockedStudentId() {
        assertEquals(0, VALIDATOR.validate(new CreateBlockResource(2L)).size());
    }
}
