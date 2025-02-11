package com.telus.credit.model;

import java.sql.Timestamp;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AccountInfo {

    private String accountType;

    private String accountSubType;

    private String status;

    private String brandId;

    private String language;

    private Timestamp statusDate;

    private Timestamp startServiceDate;

    private String statusActivityCode;

    private String statusActivityReasonCode;

    private String dealerCode;

    private String salesRepCode;

    private String corpAcctRepCode;

    private String fullName;

    private String title;

    private String firstName;

    private String middleInitial;

    private String lastName;

    private String legalBusinessName;

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountSubType() {
        return accountSubType;
    }

    public void setAccountSubType(String accountSubType) {
        this.accountSubType = accountSubType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Timestamp getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Timestamp statusDate) {
        this.statusDate = statusDate;
    }

    public Timestamp getStartServiceDate() {
        return startServiceDate;
    }

    public void setStartServiceDate(Timestamp startServiceDate) {
        this.startServiceDate = startServiceDate;
    }

    public String getStatusActivityCode() {
        return statusActivityCode;
    }

    public void setStatusActivityCode(String statusActivityCode) {
        this.statusActivityCode = statusActivityCode;
    }

    public String getStatusActivityReasonCode() {
        return statusActivityReasonCode;
    }

    public void setStatusActivityReasonCode(String statusActivityReasonCode) {
        this.statusActivityReasonCode = statusActivityReasonCode;
    }

    public String getDealerCode() {
        return dealerCode;
    }

    public void setDealerCode(String dealerCode) {
        this.dealerCode = dealerCode;
    }

    public String getSalesRepCode() {
        return salesRepCode;
    }

    public void setSalesRepCode(String salesRepCode) {
        this.salesRepCode = salesRepCode;
    }

    public String getCorpAcctRepCode() {
        return corpAcctRepCode;
    }

    public void setCorpAcctRepCode(String corpAcctRepCode) {
        this.corpAcctRepCode = corpAcctRepCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLegalBusinessName() {
        return legalBusinessName;
    }

    public void setLegalBusinessName(String legalBusinessName) {
        this.legalBusinessName = legalBusinessName;
    }
    
    @Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
}
