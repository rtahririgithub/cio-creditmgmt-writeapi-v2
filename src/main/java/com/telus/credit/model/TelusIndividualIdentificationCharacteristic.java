package com.telus.credit.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.validation.ValidCountryCode;
import com.telus.credit.validation.ValidProvinceCode;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

@JsonInclude(Include.NON_NULL)
public class TelusIndividualIdentificationCharacteristic {

  //removed ValidProvinceCode to allow free text value
  // @ValidProvinceCode(groups = {Create.class, Patch.class})
   private String provinceCd;
   private String identificationTypeCd;
   @ValidCountryCode(groups = {Create.class, Patch.class})
   private String countryCd;

   @JsonIgnore
   private boolean provinceCdDirty = false;
   @JsonIgnore
   private boolean identificationTypeCdDirty = false;
   @JsonIgnore
   private boolean countryCdDirty = false;
   
   public String getProvinceCd() {
      return provinceCd;
   }
   public void setProvinceCd(String provinceCd) {
      this.provinceCd = provinceCd;
      this.provinceCdDirty = true;
   }
   public String getIdentificationTypeCd() {
      return identificationTypeCd;
   }
   public void setIdentificationTypeCd(String identificationTypeCd) {
      this.identificationTypeCd = identificationTypeCd;
      this.identificationTypeCdDirty = true;
   }

   public String getCountryCd() {
      return countryCd;
   }
   public void setCountryCd(String countryCd) {
      this.countryCd = countryCd;
      this.countryCdDirty = true;
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getProvinceCdPatch() {
      return PatchField.patchOrNull(provinceCdDirty, provinceCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getIdentificationTypeCdPatch() {
      return PatchField.patchOrNull(identificationTypeCdDirty, identificationTypeCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getCountryCdPatch() {
      return PatchField.patchOrNull(countryCdDirty, countryCd);
   }

   @Override
	public String toString() {
      return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
