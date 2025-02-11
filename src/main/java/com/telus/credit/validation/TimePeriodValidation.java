package com.telus.credit.validation;

import java.time.format.DateTimeParseException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.model.TimePeriod;

public class TimePeriodValidation implements ConstraintValidator<ValidTimePeriod, TimePeriod> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimePeriodValidation.class);

    @Override
    public void initialize(ValidTimePeriod contactNumber) {
        // no need
    }

    @Override
    public boolean isValid(TimePeriod value, ConstraintValidatorContext cxt) {
    	String startDateTime = (value!=null && value.getStartDateTime()!=null )?value.getStartDateTime():"";
    	String endDateTime = (value!=null && value.getEndDateTime()!=null  )?value.getEndDateTime():"";

        if (startDateTime.isEmpty() 
        		|| (startDateTime.isEmpty() && endDateTime.isEmpty() ) ) {
            return true;
        }
        
        
        try {
        	//validate for a valid  value 
        	DateTimeUtils.toUtcTimestamp(startDateTime);
        	
        	//validate for a valid  value 
        	if (!endDateTime.isEmpty() ) {
        		 DateTimeUtils.toUtcTimestamp(endDateTime);
        	}
        	//validate startDate is before endDate
        	if (!endDateTime.isEmpty() )
        	{
        		boolean isTimeStampsEqual = DateTimeUtils.toUtcTimestamp(startDateTime).equals(DateTimeUtils.toUtcTimestamp(endDateTime));
        		if(isTimeStampsEqual) {
        			return true;
        		}
        		boolean isStartTsBeforeEndTs = DateTimeUtils.toUtcTimestamp(startDateTime).before(DateTimeUtils.toUtcTimestamp(endDateTime));
        		if(isStartTsBeforeEndTs) {
        			return true;
        		}else {
        			return false;
        		}       		
        	}
        	
        	return true;
        } catch (DateTimeParseException e) {
            LOGGER.warn("Invalid input {}", e.getMessage());
            return false;
        }
    }

}