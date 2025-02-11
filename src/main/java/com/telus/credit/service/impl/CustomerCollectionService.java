package com.telus.credit.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.telus.credit.exceptions.PubSubPublishException;
import com.telus.credit.model.AccountInfo;
import com.telus.credit.model.Customer;
import com.telus.credit.model.CustomerPubSub;
import com.telus.credit.model.TelusCreditProfile;

@Service
public class CustomerCollectionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerCollectionService.class);

	@Autowired
	private PubSubTemplate pubSubTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${cpsync.pubsub.topic}")
	private String topic;

	/**
	 * Publish customer to PubSub
	 *
	 * @param newCustomer
	 * @throws Exception
	 */
	public void updateCustomerCollection(Customer newCustomer, AccountInfo accountInfo, String createUpdateFlag, long eventReceivedTime, long submitterEventTime, String eventDescription) throws PubSubPublishException {
		
		
		LOGGER.debug("publish to topic: "
				+ "topic:{},  "
				+ "eventDescription:{}, "
				+ "createUpdateFlag: {} , "
				+ "receivedTime: {}, "
				+ "newCustomer::{} , "
				+ "accountInfo::{} | "
				,topic
				,eventDescription
				,createUpdateFlag
				,eventReceivedTime
				,newCustomer
				,accountInfo
				);
		if (eventReceivedTime < 1) {
			eventReceivedTime = System.currentTimeMillis();
		}
		CustomerPubSub customerPubSub = new CustomerPubSub(newCustomer, accountInfo, createUpdateFlag, eventReceivedTime,eventDescription,submitterEventTime);
		try {			
			//Note: writeValueAsString takes double the time comparing to writeValueAsBytes. and takes 50% less memory.
			byte[] customerPubSubBytes = objectMapper.writeValueAsBytes(customerPubSub);
			pubSubTemplate.publish(topic, customerPubSubBytes).get();	
			
			List<TelusCreditProfile> cpList = customerPubSub.getCreditProfile();
			for (TelusCreditProfile telusCreditProfile : cpList) {
				LOGGER.debug(objectMapper.writeValueAsString(telusCreditProfile));
			}
		} catch (Exception e) {
			throw new PubSubPublishException(e);
		}
	}	
}
