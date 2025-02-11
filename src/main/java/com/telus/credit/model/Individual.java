package com.telus.credit.model;

import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.common.PartyType;
import com.telus.credit.validation.UniqueIdentification;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

public class Individual implements RelatedPartyInterface {
	private String id;
	private String birthDate;
	private String role;
	private List<ContactMedium> contactMedium;
	private List<TelusIndividualIdentification> individualIdentification;
	@Valid
	@UniqueIdentification(groups = {Create.class, Patch.class})
	@JsonProperty("characteristic")
	private List<TelusCharacteristic> characteristic;

	@JsonIgnore
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public List<ContactMedium> getContactMedium() {
		return contactMedium;
	}

	public void setContactMedium(List<ContactMedium> contactMedium) {
		this.contactMedium = contactMedium;
	}

	public List<TelusIndividualIdentification> getIndividualIdentification() {
		return individualIdentification;
	}

	public void setIndividualIdentification(List<TelusIndividualIdentification> individualIdentification) {
		this.individualIdentification = individualIdentification;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public PartyType getRelatedPartyType() {
		return PartyType.INDIVIDUAL;
	}

	public List<TelusCharacteristic> getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(List<TelusCharacteristic> characteristic) {
		this.characteristic = characteristic;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
