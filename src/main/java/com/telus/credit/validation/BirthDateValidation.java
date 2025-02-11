package com.telus.credit.validation;

import java.time.format.DateTimeParseException;
import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telus.credit.common.DateTimeUtils;

public class BirthDateValidation implements ConstraintValidator<ValidBirthDate, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BirthDateValidation.class);

    @Override
    public void initialize(ValidBirthDate contactNumber) {
        // no need
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext cxt) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        try {
            Date date = DateTimeUtils.toUtcDate(value);
            return date.before(new Date());
        } catch (DateTimeParseException e) {
            LOGGER.warn("BirthDateValidation Invalid input.  {}", e.getMessage());
            // return true in case we cannot handle (let the other handle)
            return true;
        }
    }

}