package com.telus.credit.validation;


import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusIndividualIdentification;
import com.telus.credit.model.common.IdentificationType;

public class IdentificationValidation implements ConstraintValidator<ValidIdentification, RelatedPartyToPatch> {

	@Override
    public void initialize(ValidIdentification contactNumber) {
    	 if (contactNumber == null) {
             return ;
         }
    }

    @Override
    public boolean isValid(RelatedPartyToPatch relatedParty, ConstraintValidatorContext cxt) {
    	//validation of credit IDs is removed to allow customer data from conversion or migration, ...
    	return true;
    }
    public boolean isValidOrig(RelatedPartyToPatch relatedParty, ConstraintValidatorContext cxt) {
        if (relatedParty == null) {
            return true;
        }
        List<TelusIndividualIdentification> individualIdentifications = relatedParty.getIndividualIdentification();
        if(individualIdentifications!=null) {
	        for (TelusIndividualIdentification telusIndividualIdentification : individualIdentifications) {
	            String identificationType = telusIndividualIdentification.getIdentificationType();
	            String identificationId = telusIndividualIdentification.getIdentificationId();
	            
	            if(IdentificationType.SIN.getDesc().equalsIgnoreCase(identificationType)
	            		|| 
	            		IdentificationType.SIN.name().equalsIgnoreCase(identificationType)
	            		) {
	            	return validateSIN(identificationId);
	            }			
	            
			}
        }
        //validation removed as we need to allow creation of creditprofile without a identification
       //IndividualIdentification or OrganizationIdentification must be provided 
       // boolean identificationIsProvided = !(!CollectionUtils.isEmpty(relatedParty.getIndividualIdentification()) && !CollectionUtils.isEmpty(relatedParty.getOrganizationIdentification()));
        
        
        return true;
       // return !(!CollectionUtils.isEmpty(value.getIndividualIdentification()) && !CollectionUtils.isEmpty(value.getOrganizationIdentification()));
    }

	private boolean validateSIN(String sinStr) {
		boolean isvalid = ID_ValidationUtils.isValid_SIN_Number(sinStr);
		return isvalid;
		 
	}

}