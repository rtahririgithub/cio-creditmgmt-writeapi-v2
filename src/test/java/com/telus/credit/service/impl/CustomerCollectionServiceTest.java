package com.telus.credit.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.model.Customer;
import com.telus.credit.model.CustomerPubSub;

@ExtendWith(SpringExtension.class)
@Import(CustomerCollectionService.class)
class CustomerCollectionServiceTest {

    @MockBean
    private PubSubTemplate pubSubTemplate;

    @MockBean
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerCollectionService underTest;

 //   @Test
    void testPublishSyncData() throws Exception {
        String content = "{}";

        doReturn(mock(ListenableFuture.class)).when(pubSubTemplate).publish(anyString(), anyString());
        doReturn(content).when(objectMapper).writeValueAsString(any(CustomerPubSub.class));

        underTest.updateCustomerCollection(new Customer(), null, "U", System.currentTimeMillis(),System.currentTimeMillis(),"");
        verify(pubSubTemplate, times(1)).publish(anyString(), eq(content));
    }
}