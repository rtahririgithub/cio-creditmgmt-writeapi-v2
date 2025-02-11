package com.telus.credit.validation;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class LineOfBusinessValidation implements ConstraintValidator<ValidLineOfBusiness, String> {
    private static final String WIRELINE = "WIRELINE";
    private static final String WIRELESS = "WIRELESS";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext cxt) {
        if (WIRELINE.equals(value) || WIRELESS.equals(value)) {
            return true;
        }
        return false;
    }
}