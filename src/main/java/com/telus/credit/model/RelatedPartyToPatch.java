package com.telus.credit.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.common.PartyType;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.validation.UniqueIdentification;
import com.telus.credit.validation.ValidBirthDate;
import com.telus.credit.validation.ValidEnum;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

public class RelatedPartyToPatch {

   private static final String BIRTHDT_PT = "(19|[2-9][0-9])[0-9]{2}\\-[0-9]{2}\\-[0-9]{2}";

   private String id;
   private String role;
   @NotBlank(groups = {Create.class, Patch.class}, message = "1103")
   @ValidEnum(enumClass = PartyType.class, useToString = true, message = "1103", groups = {Create.class, Patch.class})
   private String atReferredType;
   
   //removed validation as legacy data contains invalid date and we have to support it
   //@Pattern(regexp = BIRTHDT_PT, message = "1104", groups = {Create.class, Patch.class})
   //@ValidBirthDate(groups = {Create.class, Patch.class})
   private String birthDate;
   
   @Valid
   private List<ContactMedium> contactMedium;
   @Valid
   @UniqueIdentification(groups = {Create.class, Patch.class})
   private List<TelusIndividualIdentification> individualIdentification;
   @Valid
   @UniqueIdentification(groups = {Create.class, Patch.class})
   private List<OrganizationIdentification> organizationIdentification;

   @Valid
   @UniqueIdentification(groups = {Create.class, Patch.class})
   @JsonProperty("characteristic")
   private List<TelusCharacteristic> characteristic;

   private boolean roleDirty = false;
   private boolean birthDateDirty = false;
   private boolean contactMediumDirty = false;
   private boolean individualIdentificationDirty = false;
   private boolean organizationIdentificationDirty = false;
   @JsonIgnore
   private boolean characteristicDirty = false;
   
   public String getId() {
      return id;
   }
   public void setId(String id) {
      this.id = id;
   }

   public String getRole() {
      return role;
   }

   public void setRole(String role) {
      this.role = role;
      this.roleDirty = true;
   }

   @JsonProperty("@referredType")
   public String getAtReferredType() {
      return atReferredType;
   }
   public void setAtReferredType(String atReferredType) {
      this.atReferredType = atReferredType;
   }
   
   public String getBirthDate() {
      return birthDate;
   }
   public void setBirthDate(String birthDate) {
      this.birthDate = birthDate;
      this.birthDateDirty = true;
   }
   public List<ContactMedium> getContactMedium() {
      return contactMedium;
   }
   public void setContactMedium(List<ContactMedium> contactMedium) {
      this.contactMedium = contactMedium;
      this.contactMediumDirty = true;
   }
   public List<TelusIndividualIdentification> getIndividualIdentification() {
      return individualIdentification;
   }
   public void setIndividualIdentification(List<TelusIndividualIdentification> individualIdentification) {
      this.individualIdentification = individualIdentification;
      this.individualIdentificationDirty = true;
   }
   public List<OrganizationIdentification> getOrganizationIdentification() {
      return organizationIdentification;
   }
   public void setOrganizationIdentification(List<OrganizationIdentification> organizationIdentification) {
      this.organizationIdentification = organizationIdentification;
      this.organizationIdentificationDirty = true;
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getRolePatch() {
      return PatchField.patchOrNull(roleDirty, role);
   }


   @JsonIgnore
   @Exclude
   public PatchField<String> getBirthDatePatch() {
      return PatchField.patchOrNull(birthDateDirty, birthDate);
   }

   @JsonIgnore
   @Exclude
   public PatchField<List<ContactMedium>> getContactMediumPatch() {
      return PatchField.patchOrNull(contactMediumDirty, contactMedium);
   }

   @JsonIgnore
   @Exclude
   public PatchField<List<TelusIndividualIdentification>> getIndividualIdentificationPatch() {
      return PatchField.patchOrNull(individualIdentificationDirty, individualIdentification);
   }

   @JsonIgnore
   @Exclude
   public PatchField<List<OrganizationIdentification>> getOrganizationIdentificationPatch() {
      return PatchField.patchOrNull(organizationIdentificationDirty, organizationIdentification);
   }

   @Override
	public String toString() {
      return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}

   public List<TelusCharacteristic> getCharacteristic() {
      return characteristic;
   }

   public void setCharacteristic(List<TelusCharacteristic> characteristic) {
      this.characteristic = characteristic;
      this.characteristicDirty = true;
   }

   @JsonIgnore
   @Exclude
   public PatchField<List<TelusCharacteristic>> getCharacteristicsPatch() {
      return PatchField.patchOrNull(characteristicDirty, characteristic);
   }

}
