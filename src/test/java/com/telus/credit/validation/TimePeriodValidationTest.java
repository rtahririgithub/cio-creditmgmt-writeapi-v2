package com.telus.credit.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.telus.credit.model.TimePeriod;

class TimePeriodValidationTest {

    public static final String DATE = "2000-01-09T16:51:22.620Z";
    public static final String DATE_GT = "2001-01-09T16:51:22.620Z";

    private TimePeriodValidation underTest;

    @BeforeEach
    void setup() {
        underTest = new TimePeriodValidation();
    }

    @Test
    void testBothNull() {
        TimePeriod timePeriod = new TimePeriod();
        assertTrue(underTest.isValid(timePeriod, null));
    }

    @Test
    void testEitherNull() {
        TimePeriod timePeriod = new TimePeriod();
        timePeriod.setEndDateTime(DATE);
        assertTrue(underTest.isValid(timePeriod, null));

        timePeriod = new TimePeriod();
        timePeriod.setStartDateTime(DATE);
        assertTrue(underTest.isValid(timePeriod, null));
    }

    @Test
    void testEndDateGreaterThanStartDate() {
        TimePeriod timePeriod = new TimePeriod();
        timePeriod.setEndDateTime(DATE_GT);
        timePeriod.setStartDateTime(DATE);
        assertTrue(underTest.isValid(timePeriod, null));
    }

    @Test
    void testStartDateGreaterThanEndDate() {
        TimePeriod timePeriod = new TimePeriod();
        timePeriod.setEndDateTime(DATE);
        timePeriod.setStartDateTime(DATE_GT);
        assertFalse(underTest.isValid(timePeriod, null));
    }

    @Test
    void testStartDateEqualsToEndDate() {
        TimePeriod timePeriod = new TimePeriod();
        timePeriod.setEndDateTime(DATE);
        timePeriod.setStartDateTime(DATE);
        assertFalse(underTest.isValid(timePeriod, null));
    }

    @Test
    void testParseError() {
        TimePeriod timePeriod = new TimePeriod();
        timePeriod.setEndDateTime(DATE + "fsdfsfdsf");
        timePeriod.setStartDateTime(DATE);
        assertFalse(underTest.isValid(timePeriod, null));
    }
}