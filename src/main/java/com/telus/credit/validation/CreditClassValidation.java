package com.telus.credit.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.telus.credit.pds.service.ReferenceDataService;

@Component
public class CreditClassValidation implements ConstraintValidator<ValidCreditClass, String> {

    @Autowired
    public void setReferenceDataService(ReferenceDataService referenceDataService) {
    }

    @Override
    public void initialize(ValidCreditClass annotation) {
        // no need
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext cxt) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        try {
        	//commented out to support KB's creditclass values that are not in refpds
            //referenceDataService.getCreditClass(value, EN_LANG, PdsRefConstants.DEFAULT_VALUE_CODE);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

}