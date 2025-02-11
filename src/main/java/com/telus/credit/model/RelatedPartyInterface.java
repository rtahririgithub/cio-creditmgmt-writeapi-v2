package com.telus.credit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.telus.credit.model.common.PartyType;

@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@referredType")
@JsonSubTypes({ 
  @Type(value = Individual.class, name = "Individual"), 
  @Type(value = Organization.class, name = "Organization"), 
  
})


public interface RelatedPartyInterface {

	
	
	@JsonIgnore
	PartyType getRelatedPartyType();

	@JsonProperty("@referredType")
	public String AtReferredType="";
	
	default String getAtReferredType() {
		return getRelatedPartyType().getType();
	}
	
	

}
