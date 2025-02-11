package com.telus.credit.model;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.common.PartyType;

@JsonInclude(Include.NON_NULL)
public class Organization implements RelatedPartyInterface {
	private String id;
	private String birthDate;
	private String role;
	private List<ContactMedium> contactMedium;
	private List<OrganizationIdentification> organizationIdentification;

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

	public List<OrganizationIdentification> getOrganizationIdentification() {
		return organizationIdentification;
	}

	public void setOrganizationIdentification(List<OrganizationIdentification> organizationIdentification) {
		this.organizationIdentification = organizationIdentification;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public PartyType getRelatedPartyType() {
		return PartyType.ORGANIZATION;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
