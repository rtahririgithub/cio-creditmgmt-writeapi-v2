package com.telus.credit.model;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.common.ApplicationConstants;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public class Customer extends BaseResponse {
	private static final long serialVersionUID = 1L;

	private String id;// Customer id
	private List<TelusCreditProfile> creditProfile;
	private RelatedPartyInterface engagedParty;
	
	private String custUid;

	private String schemaLocation;
	@ApiModelProperty(value = "The customer unique id", required = true, example = "16")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("@baseType")
	public String getBaseType() {
		return ApplicationConstants.CUSTOMER_BASE_TYPE;
	}

	public void setBaseType(String baseType) {
	}

	@JsonProperty("@schemaLocation")
	public String getSchemaLocation() {
		return schemaLocation;
	}

	public void setSchemaLocation(String schemaLocation) {
		this.schemaLocation = schemaLocation;
	}

	@JsonProperty("@type")
	public String getType() {
		return ApplicationConstants.CUSTOMER_TYPE;
	}

	public void setType(String type) {
	}

	public List<TelusCreditProfile> getCreditProfile() {
		return creditProfile;
	}

	public void setCreditProfile(List<TelusCreditProfile> creditProfile) {
		this.creditProfile = creditProfile;
	}

	public String getCustUid() {
		return custUid;
	}

	public void setCustUid(String custUid) {
		this.custUid = custUid;
	}

	@Exclude
	public RelatedPartyInterface getEngagedParty() {
		return engagedParty;
	}

	@Exclude
	public void setEngagedParty(RelatedPartyInterface engagedParty) {
		this.engagedParty = engagedParty;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
