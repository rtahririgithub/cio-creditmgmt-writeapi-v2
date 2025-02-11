package com.telus.credit.service.impl;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.pubsub.model.CreditAssessmentEvent;

@Service
public class CreditAssessmentMessageSender {

    public static final String TOPIC_NAME_PROPERTY_KEY = "${creditmgmt.pubsub.asmt.topicName}";

    private final PubSubTemplate pubSubTemplate;
    private final String topicName;

    private final ObjectMapper objectMapper;

    public CreditAssessmentMessageSender(PubSubTemplate pubSubTemplate,
                                         ObjectMapper objectMapper,
                                         @Value(TOPIC_NAME_PROPERTY_KEY) String topicName) {
        this.pubSubTemplate = pubSubTemplate;
        this.topicName = topicName;
        this.objectMapper = objectMapper;
    }

    public String publish(CreditAssessmentEvent event) throws ExecutionException, InterruptedException, JsonProcessingException {
        return pubSubTemplate.publish(topicName, objectMapper.writeValueAsString(event)).get();
    }
}
