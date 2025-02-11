package com.telus.credit.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.common.RequestContext;
import com.telus.credit.dao.CustomerDao;
import com.telus.credit.dao.ReadDbSyncStatusDao;
import com.telus.credit.dao.entity.CustomerEntity;
import com.telus.credit.dao.entity.ReaddbSyncStatusEntity;
import com.telus.credit.firestore.AssessmentCollectionService;
import com.telus.credit.model.CustomerToPatch;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.pubsub.service.TelusCreditProfileEventSender;
import com.telus.credit.service.CreditProfileService;
import com.telus.credit.service.CustomerService;
import com.telus.credit.service.ValidationService;


@ExtendWith(MockitoExtension.class)
class DefaultCustomerServiceTest {

    private static final Long CUST_ID = 1000L;
    private static final String CUST_UID = "37ad1c92-1111-4ccd-ad88-a1996d80a4c7";

	@Mock
    private PlatformTransactionManager platformTransactionManager;

    @Mock
    private ReadDbSyncStatusDao readDbSyncStatusDao;

    @Mock
    private ValidationService validationService;

    @Mock
    private CustomerCollectionService customerCollectionService;

    @Mock
    private CreditProfileService<TelusCreditProfile> creditProfileService;

    @Mock
    private CustomerDao customerDao;

    @Mock
    private ResponseInterceptorService responseProcessor;

    @Mock
    private AssessmentCollectionService assesmentDB;

    @Mock
    private CustomerService customerService;
    
    
    @Mock
    private TelusCreditProfileEventSender creditProfileEventSender;  

    @InjectMocks
    private DefaultCustomerService underTest;


    @Test
    void testPatchCustomerWithAllOptional() throws Exception {
        CustomerToPatch customer = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream("patch-profile-request-empty.json"), CustomerToPatch.class);

        CustomerEntity entity = new CustomerEntity();
        entity.setCustomerId(CUST_ID);
        entity.setCreditProfileCustomerId(CUST_UID);
        //doReturn(Optional.of(entity)).when(customerDao).findCustomerById(CUST_ID);
        doReturn(Optional.of(entity)).when(customerDao).findCustomerEntityByIdForUpdate(CUST_ID,"WIRELESS");

        RequestContext requestContext = mock(RequestContext.class);

        long receivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
        underTest.saveCustomerById(requestContext, CUST_ID, customer, null, receivedTime,receivedTime,"");

        verify(validationService, times(1)).validateForPatch(customer);
        verify(readDbSyncStatusDao, times(2)).update(eq(CUST_UID), any(ReaddbSyncStatusEntity.class));

        verifyNoInteractions(creditProfileService);

        //verify(customerDao, times(1)).queryForObject(any(RowMapper.class), anyString(), eq(CUST_ID));
        //verify(responseProcessor, times(1)).resolveMissingFieldsAndAudit(eq(customerResponse), eq("en"));
       // assertEquals(customerResponse, AuditService.auditContext().getResponseData());
    }
}