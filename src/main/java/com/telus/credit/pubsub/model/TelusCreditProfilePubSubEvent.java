
package com.telus.credit.pubsub.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.telus.credit.model.TelusCreditProfile;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TelusCreditProfilePubSubEvent { 

    @JsonProperty("eventId")
    private String eventId;
    
    //@JsonProperty("eventTime")
    //private String eventTime;
    
    @JsonProperty("eventType")
    private String eventType;
    @JsonProperty("correlationId")
    private String correlationId;
    @JsonProperty("description")
    private String description;
    @JsonProperty("timeOccurred")
    private String timeOccurred;
    
    @JsonProperty("event")
    private TelusCreditProfile event = null;

    /**
     * No args constructor for use in serialization
     */
    public TelusCreditProfilePubSubEvent() {
    }

    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("eventType")
    public String getEventType() {
        return eventType;
    }

    @JsonProperty("eventType")
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

	/*
	 * @JsonProperty("eventTime") public String getEventTime() { return eventTime; }
	 * 
	 * @JsonProperty("eventTime") public void setEventTime(String eventTime) {
	 * this.eventTime = eventTime; }
	 */

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("event")
    public TelusCreditProfile getEvent() {
        return event;
    }

    @JsonProperty("event")
    public void setEvent(TelusCreditProfile event) {
        this.event = event;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getTimeOccurred() {
        return timeOccurred;
    }

    public void setTimeOccurred(String timeOccurred) {
        this.timeOccurred = timeOccurred;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("eventId", eventId)
               // .append("eventTime", eventTime)
                .append("eventType", eventType)
                .append("correlationId", correlationId)
                .append("description", description)
                .append("timeOccurred", timeOccurred)
                .append("event", event)
                .toString();
    }

}
