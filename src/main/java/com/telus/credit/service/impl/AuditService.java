package com.telus.credit.service.impl;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.model.CreditProfileAuditDocument;

@Service
public class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);

    private static final ThreadLocal<CreditProfileAuditDocument> auditThreadLocal = new ThreadLocal<>();

    @Value("${auditlog.pubsub.topic}")
    private String pubsubTopic;

    @Value("${auditlog.enabled}")
    private boolean auditLogEnabled;

    @Autowired
    private PubSubTemplate pubSubTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Get audit context. Create new if it doesn't exists
     *
     * @return
     */
    public static CreditProfileAuditDocument auditContext() {
        CreditProfileAuditDocument auditDocument = auditThreadLocal.get();
        if (auditDocument == null) {
            auditThreadLocal.set(new CreditProfileAuditDocument());
        }

        return auditThreadLocal.get();
    }

    /**
     * Remove audit context
     */
    public static void resetAuditContext() {
        auditThreadLocal.remove();
    }

    /**
     * Publish audit document to PubSub
     *
     * @param auditDocument
     */
    public void addAuditLog(CreditProfileAuditDocument auditDocument) {
        if (!auditLogEnabled) {
            return;
        }

        LOGGER.debug("addAuditLog input::{}", auditDocument);
        try {
            pubSubTemplate.publish(pubsubTopic, objectMapper.writeValueAsString(auditDocument)).get();
            LOGGER.info("Audit published customerid={} {}", auditDocument.getCustomerId(), auditDocument.getEventType());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("{}: {} Audit published error customerId={} EventType={}.  {}", ExceptionConstants.STACKDRIVER_METRIC, ExceptionConstants.PUBSUB200, auditDocument.getCustomerId(), auditDocument.getEventType(), ExceptionHelper.getStackTrace(e));
        }
    }
}
