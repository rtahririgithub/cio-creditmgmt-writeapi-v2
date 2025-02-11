package com.telus.credit.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

public class ValidNumberValidation implements ConstraintValidator<ValidNumber, String> {
    private long min;
    private long max;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        try {
            long val = Long.parseLong(value);
            return val >= min && val <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    @Override
    public void initialize(ValidNumber constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
    }
}
