package com.telus.credit.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.telus.credit.pds.model.SingleKeyReferenceDataItem;
import com.telus.credit.pds.service.ReferenceDataService;

@ExtendWith(MockitoExtension.class)
class ProvinceCodeValidationTest {

    @Mock
    private ReferenceDataService referenceDataService;

    private ProvinceCodeValidation underTest;

    @BeforeEach
    void setup() {
        underTest = new ProvinceCodeValidation();
        underTest.setReferenceDataService(referenceDataService);
    }

    @Test
    public void testCodeBlank() {
        assertTrue(underTest.isValid(null, null));
        assertTrue(underTest.isValid("", null));
    }

    @Test
    public void testNotFound() {
        doThrow(IllegalArgumentException.class).when(referenceDataService).getProvinceState(anyString(), anyString(), anyString());
        assertFalse(underTest.isValid("notfound", null));
    }

    @Test
    public void testFound() {
        doReturn(new SingleKeyReferenceDataItem()).when(referenceDataService).getProvinceState(anyString(), anyString(), anyString());
        assertTrue(underTest.isValid("found", null));
    }
}