package com.telus.credit.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.telus.credit.common.PdsRefConstants;
import com.telus.credit.model.common.ApplicationConstants;
import com.telus.credit.pds.service.ReferenceDataService;

@Component
public class ProvinceCodeValidation implements ConstraintValidator<ValidProvinceCode, String> {

    private static ReferenceDataService referenceDataService;

    @Autowired
    public void setReferenceDataService(ReferenceDataService referenceDataService) {
        ProvinceCodeValidation.referenceDataService = referenceDataService;
    }

    @Override
    public void initialize(ValidProvinceCode contactNumber) {
        // no need
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext cxt) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        try {
            referenceDataService.getProvinceState(value, ApplicationConstants.EN_LANG, PdsRefConstants.DEFAULT_VALUE_CODE);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

}