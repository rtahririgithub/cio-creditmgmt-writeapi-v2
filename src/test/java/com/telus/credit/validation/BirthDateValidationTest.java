package com.telus.credit.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BirthDateValidationTest {

    private BirthDateValidation underTest;

    @BeforeEach
    void setup() {
        underTest = new BirthDateValidation();
    }

    @Test
    void testBothNull() {
        assertTrue(underTest.isValid(null, null));
        assertTrue(underTest.isValid("", null));
    }

    @Test
    void testFutureBirthDate() {
        assertFalse(underTest.isValid(DateTimeFormatter.ISO_DATE.format(LocalDate.now().plusDays(2)), null));
    }

    @Test
    void testParseError() {
        assertTrue(underTest.isValid(DateTimeFormatter.ISO_DATE.format(LocalDate.now()), null));
    }
}