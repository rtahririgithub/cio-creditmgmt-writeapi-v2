package com.telus.credit.validation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

public class EnumValidation implements ConstraintValidator<ValidEnum, String> {
    private List<String> valueList = null;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        return valueList.contains(value.toLowerCase()) || valueList.contains(value);
    }
    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        valueList = Arrays
        		.stream(constraintAnnotation.enumClass().getEnumConstants())
                .map(e -> constraintAnnotation.useToString() ? e.toString().toLowerCase() : e.name())             
                .collect(Collectors.toList());
    }
}
