package com.telus.credit.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.telus.credit.pds.service.ReferenceDataService;

public class AttachmentSizeValidation implements ConstraintValidator<ValidAttachmentSize, String> {

    @Autowired
    public void setReferenceDataService(ReferenceDataService referenceDataService) {
    }

    @Override
    public void initialize(ValidAttachmentSize contactNumber) {
        // no need
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext cxt) {
        // no implementation yet
        return true;
    }
}
