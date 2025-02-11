package com.telus.credit.service.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.telus.credit.common.LangHelper;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.dao.CreditProfileDao;
import com.telus.credit.dao.CreditWarningHistoryDao;
import com.telus.credit.dao.CustomerCreditProfileRelDao;
import com.telus.credit.model.mapper.CreditWarningHistoryModelMapper;
import com.telus.credit.model.mapper.TelusCreditProfileModelMapper;
import com.telus.credit.service.CustomerService;
import com.telus.credit.service.ValidationService;


@ExtendWith(MockitoExtension.class)
class TelusCreditProfileServiceTest {

    @Mock
    private ValidationService validationService;

    @Mock
    private CreditProfileDao creditProfileDao;

    @Mock
    private CreditWarningHistoryDao creditWarningHistoryDao;

    @Mock
    private CustomerCreditProfileRelDao customerCreditProfileRelDao;

    @Mock(lenient = true)
    private CryptoService cryptoService;

    @InjectMocks
    private TelusCreditProfileService profileService;

    private TelusCreditProfileService underTest;

    @Mock(lenient = true)
    private CustomerService customerService;

    @BeforeEach
    void setup() throws Exception {
        new LangHelper(new ResourceBundleMessageSource());

        new TelusCreditProfileModelMapper().setEncryptionService(cryptoService);
        new CreditWarningHistoryModelMapper().setEncryptionService(cryptoService);

        underTest = spy(profileService);

        doAnswer((Answer<Object>) invocation -> "ENC: " + invocation.getArgument(0)).when(cryptoService).encryptOrNull(anyString());
    }

//    @Test
//    void testCreateNewCreditProfile() throws Exception {
//        AccountInfo accountInfo = new AccountInfo();
//        Customer customer2 = new Customer();
//        RequestContext requestContext = mock(RequestContext.class);
//        long receivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
//        CustomerToPatch customer = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_FILE), CustomerToPatch.class);
//        System.out.println("CUSTOMER DATA " + customer.getCreditProfile().toString());
//        doReturn(CP_UUID).when(creditProfileDao).insert(any(CreditProfileEntity.class));
//        doReturn(customer2).when(customerService).createCustomerById(requestContext, Long.parseLong("123453"), customer.getCreditProfile().get(0), accountInfo, receivedTime, receivedTime, "");
//        doReturn(1).when(creditWarningHistoryDao).update(anyString(), any(CreditWarningHistoryEntity.class));
//
////        underTest.createCreditProfile(String.valueOf(123453), customer.getCreditProfile().get(0), customer.getTelusAuditCharacteristic());
//
////        underTest.createCreditProfileWithoutId(requestContext, customer.getCreditProfile(), accountInfo, receivedTime, receivedTime, "");
//
//        verify(validationService, times(1)).validateForCreate(customer.getCreditProfile());
//
//        ArgumentCaptor<CreditProfileEntity> entityArgumentCaptor = ArgumentCaptor.forClass(CreditProfileEntity.class);
//        verify(creditProfileDao, times(1)).insert(entityArgumentCaptor.capture());
//        TestUtils.compareObject(entityArgumentCaptor.getValue(), "entities-create/teluscreditprofile.json", UPDATE_MAP);
//
//        ArgumentCaptor<CustomerCreditProfileRelEntity> entityRelArgumentCaptor = ArgumentCaptor.forClass(CustomerCreditProfileRelEntity.class);
//        verify(customerCreditProfileRelDao, times(1)).insert(entityRelArgumentCaptor.capture());
//        TestUtils.compareObject(entityRelArgumentCaptor.getValue(), "entities-create/customer-creditprofile-rel.json", UPDATE_MAP);
//
//        ArgumentCaptor<CreditWarningHistoryEntity> entityWarnArgumentCaptor = ArgumentCaptor.forClass(CreditWarningHistoryEntity.class);
//        verify(creditWarningHistoryDao, times(1)).insert(entityWarnArgumentCaptor.capture());
//        TestUtils.compareObject(entityWarnArgumentCaptor.getValue(), "entities-create/creditwarning.json", UPDATE_MAP);
//    }
//
//    @Test
//    void testCreateNewCreditProfileWithoutId() throws Exception {
//        AccountInfo accountInfo = new AccountInfo();
//        RequestContext requestContext = mock(RequestContext.class);
//        long receivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
//
//        CreditProfileToCreate creditProfile = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream(NEW_TEST_FILE), CreditProfileToCreate.class);
//        doReturn(CP_UUID).when(creditProfileDao).insert(any(CreditProfileEntity.class));
//        System.out.println("CREATE CREDIT PROFILE " + creditProfile.getCreditProfile());
//        underTest.createCreditProfileWithoutId(requestContext, creditProfile.getCreditProfile(), accountInfo, receivedTime, receivedTime, "");
//        verify(validationService, times(1)).validateForCreate(creditProfile.getCreditProfile());
//
//        ArgumentCaptor<CreditProfileEntity> entityArgumentCaptor = ArgumentCaptor.forClass(CreditProfileEntity.class);
//        verify(creditProfileDao, times(1)).insert(entityArgumentCaptor.capture());
//        TestUtils.compareObject(entityArgumentCaptor.getValue(), "entities-create/teluscreditprofile.json", UPDATE_MAP);
//    }

	/*
	 * @Test void testPatchCreditProfile_newProfileAdded() {
	 * doNothing().when(underTest).createCreditProfile(anyString(),
	 * any(TelusCreditProfile.class), any(TelusAuditCharacteristic.class));
	 * 
	 * TelusCreditProfile cp = new TelusCreditProfile(); TelusAuditCharacteristic
	 * audit = new TelusAuditCharacteristic();
	 * underTest.patchCreditProfile(CUST_UID, cp, audit);
	 * 
	 * verify(underTest, times(1)).createCreditProfile(CUST_UID, cp, audit); }
	 */

    @Test
    void testPatchCreditProfile_NotFound() {
        //doReturn(Optional.empty()).when(creditProfileDao).getByCustomerUidAndProfileId(CUST_UID, CP_UUID);

       //elusCreditProfile creditProfile = new TelusCreditProfile();
       // creditProfile.setId(CP_UUID);
		/*
		 * assertThatThrownBy(() -> underTest.patchCreditProfile(CUST_UID,
		 * creditProfile)) .isInstanceOf(CreditException.class)
		 * .hasMessageContaining("not found");
		 */
    }




}