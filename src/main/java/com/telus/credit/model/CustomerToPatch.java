package com.telus.credit.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.validation.group.Create;

public class CustomerToPatch {

   @NotEmpty(groups = Create.class, message = "1100")
   @NotNull(groups = Create.class, message = "1100")
   @Valid
   private List<TelusCreditProfile> creditProfile;

   @Valid
   private TelusAuditCharacteristic telusAuditCharacteristic;

   private boolean creditProfileDirty = false;

   public List<TelusCreditProfile> getCreditProfile() {
      return creditProfile;
   }
   public void setCreditProfile(List<TelusCreditProfile> creditProfile) {
      this.creditProfile = creditProfile;
      this.creditProfileDirty = true;
   }

   public TelusAuditCharacteristic getTelusAuditCharacteristic() {
      return telusAuditCharacteristic;
   }
   public void setTelusAuditCharacteristic(TelusAuditCharacteristic telusAuditCharacteristic) {
      this.telusAuditCharacteristic = telusAuditCharacteristic;
   }

   @JsonIgnore
   @Exclude
   public PatchField<List<TelusCreditProfile>> getCreditProfilePatch() {
      return PatchField.patchOrNull(creditProfileDirty, creditProfile);
   }

   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
 
}
