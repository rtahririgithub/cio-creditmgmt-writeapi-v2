package com.telus.credit.pubsub.service;



import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.pubsub.model.TelusCreditProfilePubSubOutgoingEvent;



/*
    @Autowired
    private TelusCreditProfileEventSender messageSender; 
    TelusCreditProfilePubSubEvent event)
 	messageSender.publish(event);
 */
@Service
public class TelusCreditProfileEventSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelusCreditProfileEventSender.class);

    public static final String TOPIC_NAME_PROPERTY_KEY = "${creditprofileevent.pubsub.topicName}";

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;

    private final String topicName;

    public TelusCreditProfileEventSender(PubSubTemplate pubSubTemplate,
                                        ObjectMapper objectMapper,
                                        @Value(TOPIC_NAME_PROPERTY_KEY) String topicName) {
        this.pubSubTemplate = pubSubTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    /*
     * Event to contain the complete resource or just the href?  for now  the complete resource. 
     * */
    public String publish(List<TelusCreditProfile> creditProfileList, String evenType) {
    	String result = null;
    	if (creditProfileList==null || creditProfileList.isEmpty()) {
    		return result;
    	}
    	
    	long custId = creditProfileList.get(0).getRelatedPartyCustomerRoleCustId();
    	TelusCreditProfilePubSubOutgoingEvent event= new TelusCreditProfilePubSubOutgoingEvent();   	
    	
    	event.setEventType(evenType); //EventTypes are ??? CreditProfileCreateEvent  and CreditProfileAttributeValueChangeEvent
    	event.setEventId(UUID.randomUUID().toString());
     	//event.setCorrelationId(UUID.randomUUID().toString());
    	//event.setDescription(String);  	
    	event.setEventTime(DateTimeUtils.toUtcString(new Date()));
        //event.setTimeOccurred(DateTimeUtils.toUtcString(new Date()));
    	
    	event.setEvent(creditProfileList); 
    	RelatedParty relatedParty= new RelatedParty();
    	relatedParty.setId(String.valueOf(custId));
    	relatedParty.setRole("customer");
    	event.setRelatedParty(relatedParty);
        
        String payload =null;
		try {
			payload = objectMapper.writeValueAsString(event);
			result = pubSubTemplate.publish(topicName, payload).get();
		} catch (Exception e) {
			LOGGER.error("{} Error publishing TelusCreditProfilePubSubEvent message. {} ", ExceptionConstants.STACKDRIVER_METRIC, ExceptionHelper.getStackTrace(e));
		}
        LOGGER.info("CustId={}. publishresult:{}. TelusCreditProfilePubSubEventPayload:{}", custId, result, payload);
        return result;
    }
    
  
}
