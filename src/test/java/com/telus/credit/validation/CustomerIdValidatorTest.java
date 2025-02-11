package com.telus.credit.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerIdValidatorTest {

    private CustomerIdValidator idValidator;

    @BeforeEach
    void setup() {
    	idValidator = new CustomerIdValidator();
    }

    @Test
    void testValidId() {
        assertTrue(idValidator.isValid("1600", null));
    }

    @Test
    void testInValidId() {
        assertFalse(idValidator.isValid("16-00", null));
    }
}