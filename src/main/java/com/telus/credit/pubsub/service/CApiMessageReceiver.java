package com.telus.credit.pubsub.service;

import static com.telus.credit.common.CreditMgmtCommonConstants.DEBUG_CONTEXT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.telus.credit.capi.model.CreditCheckChangeEvent;
import com.telus.credit.capi.service.CApiService;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;

@Service
public class CApiMessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CApiMessageReceiver.class);

    private final CApiService capiService;

    private final JacksonPubSubMessageConverter messageConverter;

    
	@Autowired
	private  ObjectMapper objectMapper;
	
    public CApiMessageReceiver(CApiService capiService, ObjectMapper objectMapper) {
        this.capiService = capiService;
        this.messageConverter = new JacksonPubSubMessageConverter(objectMapper);
    }

    /**
     * Receives CAPI message from google pubsub topic capi-creditchange_v1.0 . Messages then will be mapped to PATCH DTO and will be process similarly
     * as requests coming from REST API
     */
    @ServiceActivator(inputChannel = "capiPubSubInputChannel")
    public void messageReceiver(@Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {      
        try {
            CreditCheckChangeEvent event = messageConverter.fromPubSubMessage(message.getPubsubMessage(), CreditCheckChangeEvent.class);       
            String custId = (event!=null)?event.getCustomerId()+"":"missing";
            MDC.put(DEBUG_CONTEXT, " CustId=" + custId);
            

            LOGGER.info ("CustId:{} , CApiMessageReceiver  event:{}",custId,objectMapper.writeValueAsString(event));  
            
            long receivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
            long submitterEventTime= DateTimeUtils.getRequestReceivedTimestampInMillis();
            capiService.sync(event,receivedTime,submitterEventTime);

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

    private void ackMessage(ListenableFuture<Void> future, String messageId) {
        try {
            future.get();
        } catch (Exception e) {
        	LOGGER.error("{}: {} Exception acknowledging pubsub message. messageId={} .  StackTrace= {}",  ExceptionConstants.STACKDRIVER_METRIC, ExceptionConstants.PUBSUB103, messageId, ExceptionHelper.getStackTrace(e));
        }
    }
}
