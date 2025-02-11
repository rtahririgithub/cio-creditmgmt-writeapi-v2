package com.telus.credit.model;

import java.sql.Timestamp;

public class CreditProfileAuditDocument {

    public enum EventType {
        CP_CREATE, CP_UPDATE,CP_MERGE,CP_UNMERGE
    }

    private String customerId;

    private EventType eventType;

    private Timestamp eventTimestamp;

    private Long eventTimestampInMillis;

    private String userId;

    private String originatingAppId;

    private String channelOrgId;

    private String correlationId;

    private String inputRequest;

    private String responseData;

    private String error;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Timestamp getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Timestamp eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
        this.eventTimestampInMillis = eventTimestamp.getTime();
    }

    public Long getEventTimestampInMillis() {
        return eventTimestampInMillis;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOriginatingAppId() {
        return originatingAppId;
    }

    public void setOriginatingAppId(String originatingAppId) {
        this.originatingAppId = originatingAppId;
    }

    public String getChannelOrgId() {
        return channelOrgId;
    }

    public void setChannelOrgId(String channelOrgId) {
        this.channelOrgId = channelOrgId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getInputRequest() {
        return inputRequest;
    }

    public void setInputRequest(String inputRequest) {
        this.inputRequest = inputRequest;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
