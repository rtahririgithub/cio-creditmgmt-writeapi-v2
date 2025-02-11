package com.telus.credit.capi.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Deposit {
    private String productType;
    private BigDecimal depositAmount;

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }
    
    @Override
   	public String toString() {
   		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
   	}
}
