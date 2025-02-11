package com.telus.credit.pubsub.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Event {

//    @JsonProperty("customer")
//    private Customer customer;
//
//    public Customer getCustomer() {
//        return customer;
//    }
//
//    public void setCustomer(Customer customer) {
//        this.customer = customer;
//    }
//
//    @Override
//    public String toString() {
//        return new ToStringBuilder(this)
//                .append("customer", customer)
//                .toString();
//    }
    @JsonProperty
    private CreditProfile creditProfile;

    public CreditProfile getCreditProfile() {
        return creditProfile;
    }

    public void setCreditProfile(CreditProfile creditProfile) {
        this.creditProfile = creditProfile;
    }

    @Override
    public String toString() {
        return new ToStringBuilder((this))
                .append("creditProfile", creditProfile)
                .toString();
    }
}
