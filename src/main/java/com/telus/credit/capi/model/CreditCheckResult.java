package com.telus.credit.capi.model;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.telus.credit.common.DtoRefectionToStringStyle;

public class CreditCheckResult {
    private Integer creditScoreNum;
    private String creditDecisionMessage;
    private String creditClass;
    private BigDecimal creditLimitAmt;

    private List<Deposit> depositList;

    public Integer getCreditScoreNum() {
        return creditScoreNum;
    }

    public void setCreditScoreNum(Integer creditScoreNum) {
        this.creditScoreNum = creditScoreNum;
    }

    public String getCreditDecisionMessage() {
        return creditDecisionMessage;
    }

    public void setCreditDecisionMessage(String creditDecisionMessage) {
        this.creditDecisionMessage = creditDecisionMessage;
    }

    public String getCreditClass() {
        return creditClass;
    }

    public void setCreditClass(String creditClass) {
        this.creditClass = creditClass;
    }

    public BigDecimal getCreditLimitAmt() {
        return creditLimitAmt;
    }

    public void setCreditLimitAmt(BigDecimal creditLimitAmt) {
        this.creditLimitAmt = creditLimitAmt;
    }

    public List<Deposit> getDepositList() {
        return depositList;
    }

    public void setDepositList(List<Deposit> depositList) {
        this.depositList = depositList;
    }
    
    @Override
    public String toString() {
       return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
 	}
}
