package com.telus.credit.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;

import javax.validation.Validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.model.CreditProfileToCreate;
import com.telus.credit.model.TelusCreditDecisionWarning;
import com.telus.credit.service.ValidationService;

@ExtendWith(MockitoExtension.class)
class WarningStatusCodeValidationTest extends AbstractValidationTest {

    private static final String NEW_TEST_FILE="create-new-credit-profile.json";

    private ValidationService underTest;

    @BeforeEach
    public void setup() {
        super.setup();
        underTest = new ValidationService(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UNASSIGNED",
            "VERIFIED",
            "UNVERIFIED",
            "INACTIVE",
            "IRRESOLVABLE",
            "REMOVED"
    })
    void testWarningStatusCode(String value) throws IOException {
        CreditProfileToCreate creditProfileToCreate = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream(NEW_TEST_FILE), CreditProfileToCreate.class);
//        CustomerToPatch customer = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_FILE), CustomerToPatch.class);

        TelusCreditDecisionWarning warning = creditProfileToCreate.getCreditProfile().getWarningHistoryList().get(0);
        warning.setWarningStatusCd(value);
        assertDoesNotThrow(() -> underTest.validateForCreate(warning));
        assertDoesNotThrow(() -> underTest.validateForPatch(warning));
    }
}