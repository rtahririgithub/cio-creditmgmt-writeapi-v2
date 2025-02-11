package com.telus.credit.service.impl;

import static org.mockito.Mockito.reset;

import org.mockito.Mock;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.telus.credit.common.LangHelper;
import com.telus.credit.pds.service.ReferenceDataService;
import com.telus.credit.validation.CountryCodeValidation;
import com.telus.credit.validation.CreditClassValidation;
import com.telus.credit.validation.CreditProgramNameValidation;
import com.telus.credit.validation.CreditWarningCategoryValidation;
import com.telus.credit.validation.DecisionCodeValidation;
import com.telus.credit.validation.ProvinceCodeValidation;

public abstract class AbstractValidationTest {
    @Mock(lenient = true)
    protected ReferenceDataService referenceDataService;

    protected void setup() {
        new LangHelper(new ResourceBundleMessageSource());
        new CountryCodeValidation().setReferenceDataService(referenceDataService);
        new ProvinceCodeValidation().setReferenceDataService(referenceDataService);
        new DecisionCodeValidation().setReferenceDataService(referenceDataService);
        new CreditClassValidation().setReferenceDataService(referenceDataService);
        new CreditProgramNameValidation().setReferenceDataService(referenceDataService);
        new CreditWarningCategoryValidation().setReferenceDataService(referenceDataService);

        reset(referenceDataService);
    }
}
