package com.telus.credit.service.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"auditlog.enabled=true"})
@Import(AuditService.class)
class AuditServiceTest {

    @MockBean
    private PubSubTemplate pubSubTemplate;

    @MockBean
    private ObjectMapper objectMapper;

    @Autowired
    private AuditService underTest;

    @Test
    void testAddAuditLog() throws JsonProcessingException {
        String content = "{}";
        AuditService.auditContext().setEventTimestamp(Timestamp.from(Instant.now()));

        doReturn(mock(ListenableFuture.class)).when(pubSubTemplate).publish(anyString(), anyString());
        doReturn(content).when(objectMapper).writeValueAsString(AuditService.auditContext());

        underTest.addAuditLog(AuditService.auditContext());
        verify(pubSubTemplate, times(1)).publish(anyString(), eq(content));
    }
}