package com.telus.credit.pubsub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.pubsub.model.XConvCustomer;
import com.telus.credit.xconv.service.XConvService;

@Service
public class XConvMessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(XConvMessageReceiver.class);

    private final XConvService xConvService;
    private final JacksonPubSubMessageConverter messageConverter;

    public XConvMessageReceiver(XConvService xConvService, ObjectMapper objectMapper) {
        this.xConvService = xConvService;
        this.messageConverter = new JacksonPubSubMessageConverter(objectMapper);
    }

    /**
     * For migration. Whenever pubsub message arrive, this service select one customer in
     * in x_credit_profile and x_warning, map it to required entities and save them.
     *
     * @param message
     */
    @ServiceActivator(inputChannel = "xConvPubSubInputChannel")
    public void messageReceiver(@Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
    	LOGGER.debug("XConvMessageReceiver Message recived");
    	
    	//TODO update x tables and migration orchestratation service to support/provide lineofbusiness as part of input
    	String conversionLineOfBusiness="WIRELINE";
        
    	ackMessage(message.ack(), message.getPubsubMessage().getMessageId());
        XConvCustomer xConvCustomer = messageConverter.fromPubSubMessage(message.getPubsubMessage(), XConvCustomer.class);
        
        try {
            if (xConvCustomer != null && xConvCustomer.getCustomer() != null) {
                MDC.put("correlationId", "CustId=" + xConvCustomer.getCustomer());
                LOGGER.debug("CustId={} , XConvMessageReceiver Processing", xConvCustomer.getCustomer());
               
                xConvService.syncCustomer(xConvCustomer.getCustomer(),conversionLineOfBusiness);
            } else {
                LOGGER.error("CustId={},{}: Invalid message: {}",xConvCustomer.getCustomer(),ExceptionConstants.STACKDRIVER_METRIC, message.getPubsubMessage());
            }
        } catch (Exception e) {
            LOGGER.error("CustId={} , {}: Exception processing pubsub message. message=: {}. {} ",xConvCustomer.getCustomer(), ExceptionConstants.STACKDRIVER_METRIC, message.getPubsubMessage(),ExceptionHelper.getStackTrace(e));
        } finally {
            MDC.clear();
        }
        LOGGER.debug("CustId={} , XConvMessageReceiver Message complete",xConvCustomer.getCustomer());
    }

    private void ackMessage(ListenableFuture<Void> future, String messageId) {
        try {
            future.get();
        } catch (Exception e) {
            LOGGER.error("{}: Exception acknowledging pubsub message. messageId= {}",ExceptionConstants.STACKDRIVER_METRIC, messageId);
        }
    }
}
