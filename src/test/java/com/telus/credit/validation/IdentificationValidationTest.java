package com.telus.credit.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.telus.credit.model.OrganizationIdentification;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusIndividualIdentification;

class IdentificationValidationTest {

    private IdentificationValidation underTest;

    @BeforeEach
    void setup() {
        underTest = new IdentificationValidation();
    }

    @Test
    void testBothNullOrEmpty() {
        assertTrue(underTest.isValid(new RelatedPartyToPatch(), null));

        RelatedPartyToPatch partyToPatch = new RelatedPartyToPatch();
        partyToPatch.setIndividualIdentification(Collections.emptyList());
        partyToPatch.setOrganizationIdentification(Collections.emptyList());
        assertTrue(underTest.isValid(partyToPatch, null));
    }

    @Test
    void testEitherNullOrEmpty() {
        RelatedPartyToPatch partyToPatch = new RelatedPartyToPatch();
        partyToPatch.setIndividualIdentification(Collections.singletonList(new TelusIndividualIdentification()));
        assertTrue(underTest.isValid(partyToPatch, null));
        partyToPatch.setOrganizationIdentification(Collections.emptyList());
        assertTrue(underTest.isValid(partyToPatch, null));

        partyToPatch = new RelatedPartyToPatch();
        partyToPatch.setOrganizationIdentification(Collections.singletonList(new OrganizationIdentification()));
        assertTrue(underTest.isValid(partyToPatch, null));
        partyToPatch.setIndividualIdentification(Collections.emptyList());
        assertTrue(underTest.isValid(partyToPatch, null));
    }


}