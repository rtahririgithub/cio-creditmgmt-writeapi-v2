package com.telus.credit.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.telus.credit.common.ErrorCode;
import com.telus.credit.exceptions.CreditException;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

@Service
public class ValidationService {

	private static final String[] REMOVE = new String[] { "Patch" };

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationService.class);

	private Validator validator;

	@Autowired
	@Lazy
	private CustomerService customerService;

	public ValidationService(Validator validator) {
		this.validator = validator;
	}

	/**
	 * Validate object for create flow
	 *
	 * @param object
	 */
	public void validateForCreate(Object object) {
		Set<ConstraintViolation<Object>> x = validator.validate(object, Create.class);
		handleError(x,object);
	}

	/**
	 * Validate object for patch flow
	 *
	 * @param object
	 */
	public void validateForPatch(Object object) {
		handleError(validator.validate(object, Patch.class),object);
	}

	private void handleError(Collection<ConstraintViolation<Object>> errors,Object object) {
		if (CollectionUtils.isEmpty(errors)) {
			return;
		}

		ArrayList<ConstraintViolation<Object>> violations = new ArrayList<>(errors);
		violations.sort(Comparator.comparing(ConstraintViolation::getMessage));

		ConstraintViolation<Object> violation = violations.get(0);
		
		throw createValidationException(
					violation.getMessage(),
					trimPath(violation.getPropertyPath()) + trimLongValue(violation.getInvalidValue()),
					object);
	}

	/**
	 * Trim long invalid value
	 *
	 * @param invalidValue
	 * @return
	 */
	private String trimLongValue(Object invalidValue) {
		String value = String.valueOf(invalidValue);
		
		return ": " + (value.length() > 200 ? value.substring(0, 200) : value);
	}

	private CreditException createValidationException(String messageCode, String message,Object object) {
		String objectStr = (object!=null)?("["+object.toString()+"]"):"";
		ErrorCode code = ErrorCode.from(messageCode);		
		if (code == null) {
			return new CreditException(HttpStatus.BAD_REQUEST, ExceptionConstants.ERR_CODE_1000, messageCode, message,objectStr);
		}

		return new CreditException(HttpStatus.BAD_REQUEST, code.code(), code.getMessage(), message,objectStr);
	}

	/**
	 * Trim the path, keep only the last element.
	 * Remove also unnecessary tokens in attribute name (in case of patch for example)
	 *
	 * @param path
	 * @return
	 */
	private String trimPath(Path path) {
		String val = path.toString();
		val = val.substring(val.lastIndexOf(".") + 1);

		for (String s : REMOVE) {
			val = val.replace(s, "");
		}

		return val;
	}

}
