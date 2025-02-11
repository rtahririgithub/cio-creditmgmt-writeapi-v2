package com.telus.credit.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.telus.credit.common.DtoRefectionToStringStyle;

@JsonInclude(Include.NON_NULL)
public class TelusOrganizationIdentificationCharacteristic {
   private String identificationTypeCd;

   public String getIdentificationTypeCd() {
      return identificationTypeCd;
   }
   public void setIdentificationTypeCd(String identificationTypeCd) {
      this.identificationTypeCd = identificationTypeCd;
   }
   
   @Override
	public String toString() {
       return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
