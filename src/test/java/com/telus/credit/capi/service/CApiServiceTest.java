package com.telus.credit.capi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.capi.mapper.CreditCheckChangeEventMapper;
import com.telus.credit.capi.model.CreditCheckChangeEvent;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.common.RequestContext;
import com.telus.credit.model.AccountInfo;
import com.telus.credit.model.CustomerToPatch;
import com.telus.credit.service.CustomerService;
import com.telus.credit.service.impl.CustomerCollectionService;

@ExtendWith(MockitoExtension.class)
class CApiServiceTest {

    //private static final Long CUST_ID = 709010181L;

    @Mock
    CustomerService customerService;

    @Mock
    CustomerCollectionService collectionService;

    @InjectMocks
    CApiService underTest;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSync() throws Exception {
    	//"CREDIT_CHECK_CREATE", "CREDIT_CHECK_UPDATE","CREDIT_CHECK_CHANGE", "CREATE_CREDIT","ACC_CANCEL","ACC_CANCEL_PORT_OUT"
    	String testfile;
    	//testfile = "capi/capievent-individual.json";
    	
    	//testfile = "capi/capievent-org_ACC_CANCEL_PORT_OUT.json";
    	//testfile = "capi/capievent-org_ACC_CANCEL.json";
    	//testfile = "capi/capievent-org_CREATE_CREDIT.json";
    	//testfile = "capi/capievent-org_CREDIT_CHECK_CHANGE.json";
    	testfile = "capi/capievent-org_CREDIT_CHECK_CREATE.json";
    	
        CreditCheckChangeEvent event = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testfile), CreditCheckChangeEvent.class);
        CustomerToPatch customerToPatch = CreditCheckChangeEventMapper.fromEvent(event);
        customerToPatch.getTelusAuditCharacteristic().setTenpubsubsync(true);
        AccountInfo accountInfo = CreditCheckChangeEventMapper.fromEvent(event.getAccount());
        long receivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
        underTest.sync(event,receivedTime,receivedTime);
        Long CUST_ID= event.getCustomerId();
        ArgumentCaptor<CustomerToPatch> customerToPatchArgumentCaptor = ArgumentCaptor.forClass(CustomerToPatch.class);
        ArgumentCaptor<AccountInfo> accountInfoArgumentCaptor = ArgumentCaptor.forClass(AccountInfo.class);
      
        try {
        	CustomerService aCustomerService = verify(customerService, times(1));
        	aCustomerService.saveCustomerById(
        			any(RequestContext.class),//requestContext, 
        			eq(CUST_ID), //custId, 
        			customerToPatchArgumentCaptor.capture(), //customerToPatch, 
        			accountInfoArgumentCaptor.capture(),//accountInfo, 
        			eq(receivedTime),
        			eq(receivedTime),
        			eq("")
        			);
		} catch (Exception e) {
			throw e;
		}
        assertThat(customerToPatch).usingRecursiveComparison().isEqualTo(customerToPatchArgumentCaptor.getValue());
        assertThat(accountInfo).usingRecursiveComparison().isEqualTo(accountInfoArgumentCaptor.getValue());
    }
}