package com.telus.credit.pubsub.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.telus.credit.model.CustomerToPatch;

/**
 * Used for ten pubsub
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Customer extends CustomerToPatch {

    @JsonProperty("id")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("customerId", id).append(super.toString())
                .toString();
    }
}
