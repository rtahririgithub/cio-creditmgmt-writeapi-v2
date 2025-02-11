package com.telus.credit.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerIdValidator implements ConstraintValidator<ValidCustomerId, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerIdValidator.class);

	@Override
	public void initialize(ValidCustomerId customerId) {
		// Default implementation
	}

	@Override
	public boolean isValid(String customerId, ConstraintValidatorContext cxt) {

		if (StringUtils.isBlank(customerId) || NumberUtils.toLong(customerId) <= 0) {
			LOGGER.warn("customerId validation failed for customerId:{}",customerId);
			return false;
		}
		return true;
	}

}