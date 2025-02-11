package com.telus.credit.pubsub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.telus.credit.model.TelusCreditProfile;

/**
 * Used for ten pubsub
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditProfile extends TelusCreditProfile {

}