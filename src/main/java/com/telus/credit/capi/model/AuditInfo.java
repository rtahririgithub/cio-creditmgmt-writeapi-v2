package com.telus.credit.capi.model;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AuditInfo {
    @NotBlank(message = "3001")
    private String originatorApplicationId;

    @NotBlank(message = "3002")
    private String kbUserId;

    private String salesRepCode;
    private String dealerCode;

    public String getOriginatorApplicationId() {
        return originatorApplicationId;
    }

    public void setOriginatorApplicationId(String originatorApplicationId) {
        this.originatorApplicationId = originatorApplicationId;
    }

    public String getKbUserId() {
        return kbUserId;
    }

    public void setKbUserId(String kbUserId) {
        this.kbUserId = kbUserId;
    }

    public String getSalesRepCode() {
        return salesRepCode;
    }

    public void setSalesRepCode(String salesRepCode) {
        this.salesRepCode = salesRepCode;
    }

    public String getDealerCode() {
        return dealerCode;
    }

    public void setDealerCode(String dealerCode) {
        this.dealerCode = dealerCode;
    }
    
    @Override
   	public String toString() {
   		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
   	}
}
