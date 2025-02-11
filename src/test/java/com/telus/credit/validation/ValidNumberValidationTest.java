package com.telus.credit.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;

import javax.validation.Payload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidNumberValidationTest {

    private ValidNumberValidation underTest;

    @BeforeEach
    void setup() {
        underTest = new ValidNumberValidation();
        underTest.initialize(new ValidNumber() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return ValidNumber.class;
            }

            @Override
            public long min() {
                return Long.MIN_VALUE;
            }

            @Override
            public long max() {
                return Long.MAX_VALUE;
            }

            @Override
            public String message() {
                return "";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }
        });
    }

    @Test
    void testBlank() {
        assertTrue(underTest.isValid(null, null));
        assertTrue(underTest.isValid("", null));
    }

    @Test
    void testParsable() {
        assertTrue(underTest.isValid(Long.toString(Long.MIN_VALUE), null));
        assertTrue(underTest.isValid(Long.toString(Long.MAX_VALUE), null));
        assertTrue(underTest.isValid(Long.toString(0), null));
    }

    @Test
    void testParseError() {
        assertFalse(underTest.isValid("string", null));
    }
}