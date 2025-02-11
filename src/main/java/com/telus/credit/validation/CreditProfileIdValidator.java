package com.telus.credit.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditProfileIdValidator implements ConstraintValidator<ValidCreditProfileId, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreditProfileIdValidator.class);

    @Override
    public void initialize(ValidCreditProfileId creditProfileId) {
        // Default implementation
    }

    @Override
    public boolean isValid(String creditProfileId, ConstraintValidatorContext cxt) {

        if (StringUtils.isBlank(creditProfileId)) {
            LOGGER.warn("creditProfileId validation failed for value:{}",creditProfileId);
            return false;
        }
        return true;
    }
}
