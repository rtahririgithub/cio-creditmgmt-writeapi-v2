package com.telus.credit.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.telus.credit.model.OrganizationIdentification;
import com.telus.credit.model.TelusIndividualIdentification;
import com.telus.credit.model.common.IdentificationType;

class UniqueIdentificationValidationTest {

    private UniqueIdentificationValidation underTest;

    @BeforeEach
    void setup() {
        underTest = new UniqueIdentificationValidation();
    }

    @Test
    void testNullOrEmpty() {
        assertTrue(underTest.isValid(null, null));
        assertTrue(underTest.isValid(Collections.emptyList(), null));
    }

    @Test
    void testSingleIndividualIdentification() {
        TelusIndividualIdentification identification = new TelusIndividualIdentification();
        identification.setIdentificationType(IdentificationType.PSP.getDesc());
        assertTrue(underTest.isValid(Collections.singletonList(identification), null));
    }

    @Test
    void testMultipleDuplicateIndividualIdentifications() {
        TelusIndividualIdentification identification1 = new TelusIndividualIdentification();
        identification1.setIdentificationType(IdentificationType.PSP.getDesc());

        TelusIndividualIdentification identification2 = new TelusIndividualIdentification();
        identification2.setIdentificationType(IdentificationType.PSP.getDesc());

        assertFalse(underTest.isValid(Arrays.asList(identification1, identification2), null));
    }

    @Test
    void testMultipleUniqueIndividualIdentifications() {
        TelusIndividualIdentification identification1 = new TelusIndividualIdentification();
        identification1.setIdentificationType(IdentificationType.PSP.getDesc());

        TelusIndividualIdentification identification2 = new TelusIndividualIdentification();
        identification2.setIdentificationType(IdentificationType.PRV.getDesc());

        assertTrue(underTest.isValid(Arrays.asList(identification1, identification2), null));
    }

    @Test
    void testMultipleNullIndividualIdentifications() {
        TelusIndividualIdentification identification1 = new TelusIndividualIdentification();
        identification1.setIdentificationType(null);

        TelusIndividualIdentification identification2 = new TelusIndividualIdentification();
        identification2.setIdentificationType(null);

        assertTrue(underTest.isValid(Arrays.asList(identification1, identification2), null));
    }

    @Test
    void testSingleOrganizationIdentificationValue() {
        OrganizationIdentification identification = new OrganizationIdentification();
        identification.setIdentificationType(IdentificationType.PSP.getDesc());
        assertTrue(underTest.isValid(Collections.singletonList(identification), null));
    }

    @Test
    void testMultipleDuplicateOrganizationIdentifications() {
        OrganizationIdentification identification1 = new OrganizationIdentification();
        identification1.setIdentificationType(IdentificationType.PSP.getDesc());

        OrganizationIdentification identification2 = new OrganizationIdentification();
        identification2.setIdentificationType(IdentificationType.PSP.getDesc());

        assertFalse(underTest.isValid(Arrays.asList(identification1, identification2), null));
    }

    @Test
    void testMultipleUniqueOrganizationIdentifications() {
        OrganizationIdentification identification1 = new OrganizationIdentification();
        identification1.setIdentificationType(IdentificationType.PSP.getDesc());

        OrganizationIdentification identification2 = new OrganizationIdentification();
        identification2.setIdentificationType(IdentificationType.PRV.getDesc());

        assertTrue(underTest.isValid(Arrays.asList(identification1, identification2), null));
    }

    @Test
    void testMultipleNullOrganizationIdentifications() {
        OrganizationIdentification identification1 = new OrganizationIdentification();
        identification1.setIdentificationType(null);

        OrganizationIdentification identification2 = new OrganizationIdentification();
        identification2.setIdentificationType(null);

        assertTrue(underTest.isValid(Arrays.asList(identification1, identification2), null));
    }
}