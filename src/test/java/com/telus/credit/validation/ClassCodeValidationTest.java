package com.telus.credit.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.telus.credit.pds.service.ReferenceDataService;

@ExtendWith(MockitoExtension.class)
class ClassCodeValidationTest {

    @Mock
    private ReferenceDataService referenceDataService;

    private CreditClassValidation underTest;

    @BeforeEach
    void setup() {
        underTest = new CreditClassValidation();
        underTest.setReferenceDataService(referenceDataService);
    }

    @Test
    public void testCodeBlank() {
        assertTrue(underTest.isValid(null, null));
        assertTrue(underTest.isValid("", null));
    }
    /*
    @Test
    public void testNotFound() {
        doThrow(IllegalArgumentException.class).when(referenceDataService).getCreditClass(anyString(), anyString(), anyString());
        assertFalse(underTest.isValid("notfound", null));
    }
  */  
/*
    @Test
    public void testFound() {
        doReturn(new SingleKeyReferenceDataItem()).when(referenceDataService).getCreditClass(anyString(), anyString(), anyString());
        assertTrue(underTest.isValid("found", null));
    }
    */
}