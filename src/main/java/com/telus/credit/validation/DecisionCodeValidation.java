package com.telus.credit.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.telus.credit.pds.service.ReferenceDataService;

@Component
public class DecisionCodeValidation implements ConstraintValidator<ValidDecisionCode, String> {

    @Autowired
    public void setReferenceDataService(ReferenceDataService referenceDataService) {
    }

    @Override
    public void initialize(ValidDecisionCode annotation) {
        // no need
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext cxt) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
//Note: Disabled BUREAU_DECISION_CODE validation as an invalid/non-existing BUREAU_DECISION_CODE shall not stop the transaction.
/*        
        try {
            List<Key> keys = MultiKeyReferenceDataService.createKeyList(PdsRefConstants.BUREAU_DECISION_CODE, value);
            MultiKeyReferenceDataItem aCreditDecisionRule = referenceDataService.getCreditDecisionRule(keys);
            if(aCreditDecisionRule == null) {
        	  return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
*/
        return true;
    }

}