package com.telus.credit.model;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.common.ApplicationConstants;
import com.telus.credit.model.common.IdentificationType;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.validation.ValidEnum;
import com.telus.credit.validation.ValidTimePeriod;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

@JsonInclude(Include.NON_NULL)
public class OrganizationIdentification implements IdentificationInterface {
	@NotBlank(groups = Create.class, message = "1123")
	private String identificationId;
	private String identificationIdHashed;
	@ValidEnum(enumClass = IdentificationType.class, useToString = true, groups = {Create.class, Patch.class}, message = "1111")
	@NotBlank(groups = {Create.class, Patch.class}, message = "1111" )
	private String identificationType;
	private String issuingAuthority;
	private String issuingDate;
	@ValidTimePeriod(groups = {Create.class, Patch.class})
	private TimePeriod validFor;

	@JsonIgnore
	private boolean identificationIdDirty = false;
	@JsonIgnore
	private boolean identificationTypeDirty = false;
	@JsonIgnore
	private boolean issuingAuthorityDirty = false;
	@JsonIgnore
	private boolean issuingDateDirty = false;
	@JsonIgnore
	private boolean validForDirty = false;

	@JsonProperty("@baseType")
	public String getBaseType() {
		return ApplicationConstants.ORGANIZATION_IDENTIFICATION_BASE_TYPE;
	}

	public void setBaseType(String baseType) {
	}
	private String schemaLocation;
	@JsonProperty("@schemaLocation")
	@JsonIgnore
	@Exclude
	public String getSchemaLocation() {
		return schemaLocation;
	}

	@Exclude
	public void setSchemaLocation(String schemaLocation) {
		this.schemaLocation = schemaLocation;
	}

	@JsonProperty("@type")
	public String getType() {
		return ApplicationConstants.ORGANIZATION_IDENTIFICATION_TYPE;
	}

	public void setType(String type) {
	}
	
	public String getIdentificationId() {
		return identificationId;
	}

	public void setIdentificationId(String identificationId) {
		this.identificationId = identificationId;
		this.identificationIdDirty = true;
	}


	public String getIdentificationIdHashed() {
		return identificationIdHashed;
	}

	public void setIdentificationIdHashed(String identificationIdHashed) {
		this.identificationIdHashed = identificationIdHashed;
	}

	public String getIdentificationType() {
		return identificationType;
	}

	public void setIdentificationType(String identificationType) {
		this.identificationType = identificationType;
		this.identificationTypeDirty = true;
	}

	public String getIssuingAuthority() {
		return issuingAuthority;
	}

	public void setIssuingAuthority(String issuingAuthority) {
		this.issuingAuthority = issuingAuthority;
		this.issuingAuthorityDirty = true;
	}

	public String getIssuingDate() {
		return issuingDate;
	}

	public void setIssuingDate(String issuingDate) {
		this.issuingDate = issuingDate;
		this.issuingDateDirty = true;
	}

	public TimePeriod getValidFor() {
		return validFor;
	}

	public void setValidFor(TimePeriod validFor) {
		this.validFor = validFor;
		this.validForDirty = true;
	}

	@JsonIgnore
	@Exclude
	public PatchField<@NotBlank(groups = Patch.class, message = "1123") String> getIdentificationIdPatch() {
		return PatchField.patchOrNull(identificationIdDirty, identificationId);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getIdentificationTypePatch() {
		return PatchField.patchOrNull(identificationTypeDirty, identificationType);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getIssuingAuthorityPatch() {
		return PatchField.patchOrNull(issuingAuthorityDirty, issuingAuthority);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getIssuingDatePatch() {
		return PatchField.patchOrNull(issuingDateDirty, issuingDate);
	}

	@JsonIgnore
	@Exclude
	public PatchField<TimePeriod> getValidForPatch() {
		return PatchField.patchOrNull(validForDirty, validFor);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
