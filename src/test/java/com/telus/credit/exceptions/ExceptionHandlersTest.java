package com.telus.credit.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.common.LangHelper;
import com.telus.credit.model.CreditProfileAuditDocument;
import com.telus.credit.model.ErrorResponse;
import com.telus.credit.service.impl.AuditService;

@ExtendWith(MockitoExtension.class)
@Disabled
class ExceptionHandlersTest {

    @Mock
    private AuditService auditService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExceptionHandlers exceptionHandlers;

    @BeforeEach
    void setup() {
        new LangHelper(new ResourceBundleMessageSource());
        AuditService.auditContext().setError(null);
    }

    @Test
    void testHandleDataAccessException() throws JsonProcessingException {
        AuditService.auditContext().setEventType(CreditProfileAuditDocument.EventType.CP_UPDATE);
        String errMsg = "Data Error";
        doReturn(errMsg).when(objectMapper).writeValueAsString(any(ErrorResponse.class));

        DataIntegrityViolationException de = new DataIntegrityViolationException("aaaa");
        exceptionHandlers.handleDataAccessException(de);
        assertEquals(errMsg, AuditService.auditContext().getError());
    }

    @Test
    void testHandleException() throws JsonProcessingException {
        AuditService.auditContext().setEventType(CreditProfileAuditDocument.EventType.CP_CREATE);
        String errMsg = "Error";
        doReturn(errMsg).when(objectMapper).writeValueAsString(any(ErrorResponse.class));

        Exception e = new RuntimeException("aaaa");
       // exceptionHandlers.handleException(e);
        //assertEquals(errMsg, AuditService.auditContext().getError());
    }

    @Test
    void testHandleCreditBadRequestException() {
        AuditService.auditContext().setEventType(CreditProfileAuditDocument.EventType.CP_UPDATE);
        CreditException e = new CreditException(HttpStatus.BAD_REQUEST, "1", "1", "aaaa");
        exceptionHandlers.handleCreditException(e);
        assertNull(AuditService.auditContext().getError());
    }

    @Test
    void testHandleOtherCreditExceptions() throws JsonProcessingException {
        AuditService.auditContext().setEventType(CreditProfileAuditDocument.EventType.CP_CREATE);
        String errMsg = "CError";
        doReturn(errMsg).when(objectMapper).writeValueAsString(any(ErrorResponse.class));

        CreditException e = new CreditException(HttpStatus.INTERNAL_SERVER_ERROR, "1", "1", "aaaa");
        exceptionHandlers.handleCreditException(e);
        assertEquals(errMsg, AuditService.auditContext().getError());
    }

    @Test
    void testHandleDataAccessExceptionNoPersist() {
        DataIntegrityViolationException de = new DataIntegrityViolationException("aaaa");
        //exceptionHandlers.handleDataAccessException(de);
        //assertNull(AuditService.auditContext().getError());
    }

    @Test
    void testHandleExceptionNoPersist() {
        Exception e = new RuntimeException("aaaa");
        //exceptionHandlers.handleException(e);
        //assertNull(AuditService.auditContext().getError());
    }

    @Test
    void testHandleCreditBadRequestExceptionNoPersist() {
        CreditException e = new CreditException(HttpStatus.INTERNAL_SERVER_ERROR, "1", "1", "aaaa");
        exceptionHandlers.handleCreditException(e);
        assertNull(AuditService.auditContext().getError());
    }
}