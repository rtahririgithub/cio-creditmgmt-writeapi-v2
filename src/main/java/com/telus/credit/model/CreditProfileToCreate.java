package com.telus.credit.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.validation.group.Create;

@JsonInclude(Include.NON_NULL)
public class CreditProfileToCreate {

//    @NotEmpty(groups = Create.class, message = "1100")
    @NotNull(groups = Create.class, message = "1100")
    @Valid
    private TelusCreditProfile creditProfile;

    public TelusCreditProfile getCreditProfile() {
        return creditProfile;
    }


    @Valid
    private TelusAuditCharacteristic telusAuditCharacteristic;

    public TelusAuditCharacteristic getTelusAuditCharacteristic() {
        return telusAuditCharacteristic;
    }
    public void setTelusAuditCharacteristic(TelusAuditCharacteristic telusAuditCharacteristic) {
        this.telusAuditCharacteristic = telusAuditCharacteristic;
    }

    public void setCreditProfile(TelusCreditProfile creditProfile) {
        this.creditProfile = creditProfile;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
    }

}
