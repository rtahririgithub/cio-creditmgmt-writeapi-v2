package com.telus.credit.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.validation.ValidCountryCode;
import com.telus.credit.validation.ValidProvinceCode;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

public class MediumCharacteristic {
	private String city;
	private String contactType;
	@ValidCountryCode(groups = {Create.class, Patch.class})
	private String country;
	private String postCode;
	//removed ValidProvinceCode to allow free text value
	//@ValidProvinceCode(groups = {Create.class, Patch.class})
	private String stateOrProvince;
	private String street1;
	private String street2;
	private String street3;
	private String street4;
	private String street5;
	private String email;
	private String phoneNumber;

	@JsonIgnore
	private boolean cityDirty = false;
	@JsonIgnore
	private boolean contactTypeDirty = false;
	@JsonIgnore
	private boolean countryDirty = false;
	@JsonIgnore
	private boolean postCodeDirty = false;
	@JsonIgnore
	private boolean stateOrProvinceDirty = false;
	@JsonIgnore
	private boolean street1Dirty = false;
	@JsonIgnore
	private boolean street2Dirty = false;
	@JsonIgnore
	private boolean street3Dirty = false;
	@JsonIgnore
	private boolean street4Dirty = false;
	@JsonIgnore
	private boolean street5Dirty = false;
	@JsonIgnore
	private boolean emailDirty = false;
	@JsonIgnore
	private boolean phoneNumberDirty = false;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
		this.cityDirty = true;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
		this.contactTypeDirty = true;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
		this.countryDirty = true;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
		this.postCodeDirty = true;
	}

	public String getStateOrProvince() {
		return stateOrProvince;
	}

	public void setStateOrProvince(String stateOrProvince) {
		this.stateOrProvince = stateOrProvince;
		this.stateOrProvinceDirty = true;
	}

	public String getStreet1() {
		return street1;
	}

	public void setStreet1(String street1) {
		this.street1 = street1;
		this.street1Dirty = true;
	}

	public String getStreet2() {
		return street2;
	}

	public void setStreet2(String street2) {
		this.street2 = street2;
		this.street2Dirty = true;
	}

	public String getStreet3() {
		return street3;
	}

	public void setStreet3(String street3) {
		this.street3 = street3;
		this.street3Dirty = true;
	}

	public String getStreet4() {
		return street4;
	}

	public void setStreet4(String street4) {
		this.street4 = street4;
		this.street4Dirty = true;
	}

	public String getStreet5() {
		return street5;
	}

	public void setStreet5(String street5) {
		this.street5 = street5;
		this.street5Dirty = true;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
		this.emailDirty = true;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		this.phoneNumberDirty = true;
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getCityPatch() {
		return PatchField.patchOrNull(cityDirty, city);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getContactTypePatch() {
		return PatchField.patchOrNull(contactTypeDirty, contactType);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getCountryPatch() {
		return PatchField.patchOrNull(countryDirty, country);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getPostCodePatch() {
		return PatchField.patchOrNull(postCodeDirty, postCode);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getStateOrProvincePatch() {
		return PatchField.patchOrNull(stateOrProvinceDirty, stateOrProvince);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getStreet1Patch() {
		return PatchField.patchOrNull(street1Dirty, street1);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getStreet2Patch() {
		return PatchField.patchOrNull(street2Dirty, street2);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getStreet3Patch() {
		return PatchField.patchOrNull(street3Dirty, street3);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getStreet4Patch() {
		return PatchField.patchOrNull(street4Dirty, street4);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getStreet5Patch() {
		return PatchField.patchOrNull(street5Dirty, street5);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getEmailPatch() {
		return PatchField.patchOrNull(emailDirty, email);
	}

	@JsonIgnore
	@Exclude
	public PatchField<String> getPhoneNumberPatch() {
		return PatchField.patchOrNull(phoneNumberDirty, phoneNumber);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
