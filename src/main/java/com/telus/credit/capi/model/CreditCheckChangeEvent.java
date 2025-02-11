package com.telus.credit.capi.model;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCheckChangeEvent {
	
	
    private String eventId;

    private String eventTimestamp;

   // @Pattern(regexp = "CREDIT_CHECK_CHANGE", message = "3000")
   // @NotBlank(message = "3000")
    private String eventType;
    private String transactionDate;
    private String notificationSuppressionInd;

    @NotNull(message = "3001")
    private AuditInfo auditInfo;

    @NotNull(message = "3003")
    private Account account;

    private CreditCheckResult creditCheckResult;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(String eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNotificationSuppressionInd() {
        return notificationSuppressionInd;
    }

    public void setNotificationSuppressionInd(String notificationSuppressionInd) {
        this.notificationSuppressionInd = notificationSuppressionInd;
    }

    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfo auditInfo) {
        this.auditInfo = auditInfo;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public CreditCheckResult getCreditCheckResult() {
        return creditCheckResult;
    }

    public void setCreditCheckResult(CreditCheckResult creditCheckResult) {
        this.creditCheckResult = creditCheckResult;
    }

    public Long getCustomerId() {
        return account != null ? account.getBan() : null;
    }
   
    @Override
   	public String toString() {
   		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
   	}
}
