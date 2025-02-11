package com.telus.credit.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.telus.credit.pds.service.ReferenceDataService;

@ExtendWith(MockitoExtension.class)
class DecisionCodeValidationTest {

    @Mock
    private ReferenceDataService referenceDataService;

    private DecisionCodeValidation underTest;

    @BeforeEach
    void setup() {
        underTest = new DecisionCodeValidation();
        underTest.setReferenceDataService(referenceDataService);
    }

    @Test
    public void testCodeBlank() {
        assertTrue(underTest.isValid(null, null));
        assertTrue(underTest.isValid("", null));
    }

	/*
	 * @Test public void testNotFound() {
	 * doThrow(IllegalArgumentException.class).when(referenceDataService).
	 * getCreditDecisionRule(anyList()); assertFalse(underTest.isValid("notfound",
	 * null)); }
	 */

	/*
	 * @Test public void testFound() { doReturn(new
	 * MultiKeyReferenceDataItem()).when(referenceDataService).getCreditDecisionRule
	 * (anyList()); assertTrue(underTest.isValid("found", null)); }
	 */
}