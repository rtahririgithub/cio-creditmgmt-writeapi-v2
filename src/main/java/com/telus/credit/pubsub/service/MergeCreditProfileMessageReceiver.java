package com.telus.credit.pubsub.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter;
import org.springframework.dao.DataAccessException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.service.MergeService;


@Service
public class MergeCreditProfileMessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeCreditProfileMessageReceiver.class);
    
    private final MergeService mergeService;

    private final JacksonPubSubMessageConverter messageConverter;
    
	
    public MergeCreditProfileMessageReceiver(
    		MergeService mergeService,
    		ObjectMapper objectMapper) {
        this.messageConverter = new JacksonPubSubMessageConverter(objectMapper);
        this.mergeService=mergeService;
    }

/*
    @ServiceActivator(inputChannel = "mergecpPubSubInputChannel")
    public void messageReceiver1(@Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {      
        try {
             com.telus.credit.pubsub.model.CreditProfile inputCreditprofile =messageConverter.fromPubSubMessage(message.getPubsubMessage(), com.telus.credit.pubsub.model.CreditProfile.class);  
            String custId = inputCreditprofile.getCustomerRelatedParty().getId();
            MDC.put(DEBUG_CONTEXT, " CustId=" + custId);

            LOGGER.info ("CustId:{} , Merge CreditProfile MessageReceiver  event:{}",custId,objectMapper.writeValueAsString(message));  
            
            long receivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
            long submitterEventTime = Instant.ofEpochSecond(message.getPubsubMessage().getPublishTime().getSeconds(), message.getPubsubMessage().getPublishTime().getNanos()).toEpochMilli();    
            
            mergeService.mergeCreditprofiles(inputCreditprofile,submitterEventTime,receivedTime);

             
            ackMessage(message.ack(), message.getPubsubMessage().getMessageId());            
        } catch (Exception e) {
            if (e instanceof DataAccessException || e instanceof CannotCreateTransactionException) {
                LOGGER.error("{}: {} Database error. {}, StackTrace={} ",  ExceptionConstants.STACKDRIVER_METRIC, ExceptionConstants.POSTGRES100,ExceptionHelper.getStackTrace(e));
            } else {
               
            	LOGGER.error("{}: {} Exception processing pubsub message. message={} . errorMessage={}. StackTrace={}",  ExceptionConstants.STACKDRIVER_METRIC, ExceptionConstants.PUBSUB100, message.getPubsubMessage(), e.getMessage(),ExceptionHelper.getStackTrace(e));
            }


            ackMessage(message.nack(), message.getPubsubMessage().getMessageId());
        } finally {
            MDC.clear();
        }
    }
    */

    @ServiceActivator(inputChannel = "mergecpPubSubInputChannel")
    public void messageReceiver(@Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {     
    	LOGGER.info("Start MergeCreditProfileMessageReceiver messageReceiver ");
        try {
 
           	//long running time of mergeCreditprofiles may cause subscriber to pull the message again before mergeCreditprofiles is finished 
        	ackMessage(message.ack(), message.getPubsubMessage().getMessageId());  
        	
        	long receivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
            long submitterEventTime = Instant.ofEpochSecond(message.getPubsubMessage().getPublishTime().getSeconds(), message.getPubsubMessage().getPublishTime().getNanos()).toEpochMilli();    
            
            com.telus.credit.pubsub.model.MergeMessage mergeMessage =messageConverter.fromPubSubMessage(message.getPubsubMessage(), com.telus.credit.pubsub.model.MergeMessage.class);  
            mergeService.searchAndMergeCreditProfiles(mergeMessage,submitterEventTime,receivedTime);

        } catch (Exception e) {
            if (e instanceof DataAccessException || e instanceof CannotCreateTransactionException) {
                LOGGER.error("{}: {} Database error. {}, StackTrace={} ",  ExceptionConstants.STACKDRIVER_METRIC, ExceptionConstants.POSTGRES100,ExceptionHelper.getStackTrace(e));
            } else {
               
            	LOGGER.error("{}: {} Exception processing pubsub message. errorMessage={}. StackTrace={} . PubsubMessage={}",  
            				ExceptionConstants.STACKDRIVER_METRIC, 
            				ExceptionConstants.PUBSUB100,             				 
            				e.getMessage(),
            				ExceptionHelper.getStackTrace(e),
            				message.getPubsubMessage()
            				);
            }

            ackMessage(message.nack(), message.getPubsubMessage().getMessageId());
        } finally {
            MDC.clear();
        }
        LOGGER.info("End MergeCreditProfileMessageReceiver messageReceiver ");
    }
    
    private void ackMessage(ListenableFuture<Void> future, String messageId) {
        try {
            future.get();
        } catch (Exception e) {
        	LOGGER.error("{}: {} Exception acknowledging pubsub message. messageId={} .  StackTrace= {}",  ExceptionConstants.STACKDRIVER_METRIC, ExceptionConstants.PUBSUB103, messageId, ExceptionHelper.getStackTrace(e));
        }
    }
}
