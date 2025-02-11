package com.telus.credit.model;

import javax.validation.Valid;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.helper.PatchField;


@JsonInclude(Include.NON_NULL)
public class TelusCreditProfile extends CreditProfile {
	

	   
	@Valid
	private TelusCreditProfileCharacteristic telusCharacteristic;

	@JsonIgnore
	private boolean telusCharacteristicDirty = false;

	public TelusCreditProfileCharacteristic getTelusCharacteristic() {
		return telusCharacteristic;
	}

	public void setTelusCharacteristic(TelusCreditProfileCharacteristic telusCharacteristic) {
		this.telusCharacteristic = telusCharacteristic;
		this.telusCharacteristicDirty = true;
	}

	@JsonIgnore
	@Exclude
	public PatchField<TelusCreditProfileCharacteristic> getTelusCharacteristicPatch() {
		return PatchField.patchOrNull(telusCharacteristicDirty, telusCharacteristic);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}


	@JsonIgnore
	public long getRelatedPartyCustomerRoleCustId() {
		long relatedPartyCustomerRoleCustId=0;
		for (RelatedParty relatedParty : getRelatedParties()) {				
        	boolean isCustomerRole= ("customer".equalsIgnoreCase( relatedParty.getRole()));
        	if(isCustomerRole) {
        		relatedPartyCustomerRoleCustId=Long.parseLong(relatedParty.getId());
        		break;
        	}
        }

		return relatedPartyCustomerRoleCustId;
	}

	@JsonIgnore
	public RelatedParty getCustomerRelatedParty() {
		if(getRelatedParties()==null) {
			return null;
		}
		for (RelatedParty relatedParty : getRelatedParties()) {				
        	boolean isCustomerRole= ("customer".equalsIgnoreCase( relatedParty.getRole()));
        	if(isCustomerRole) {
        		return relatedParty;
        	}
        }
		return null;

}	
}
