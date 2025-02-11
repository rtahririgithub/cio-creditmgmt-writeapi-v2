package com.telus.credit.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.telus.credit.common.PdsRefConstants;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.model.Customer;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.model.TelusCreditProfileCharacteristic;
import com.telus.credit.model.common.ApplicationConstants;
import com.telus.credit.pds.model.MultiKeyReferenceDataItem;
import com.telus.credit.pds.model.Value;
import com.telus.credit.pds.service.ReferenceDataService;


@ExtendWith(MockitoExtension.class)
class ResponseInterceptorServiceTest {

    @Mock
    private CryptoService cryptoService;

    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    private ResponseInterceptorService underTest;

//    @Test
//    void testDecryptCreditProfile() throws Exception {
//        String enc = "enc: ";
//
//        doAnswer((Answer<Object>) invocation -> {
//            Object argument = invocation.getArgument(0);
//            return argument != null ? argument.toString().replace(enc, "") : null;
//        }).when(cryptoService).decryptAndIgnoreError(anyString());
//
//        TelusCreditProfile cp = new TelusCreditProfile();
//        cp.setCreditScore(enc + "score");
//        cp.setCreditRiskRating(enc + "setCreditRiskRating");
//
//        TelusCreditProfileCharacteristic characteristic = new TelusCreditProfileCharacteristic();
//        characteristic.setCreditClassCd(enc + "setCreditClassCd");
//        characteristic.setCreditDecisionCd(enc + "setCreditDecisionCd");
//        characteristic.setRiskLevelDecisionCd(enc + "setRiskLevelDecisionCd");
//        cp.setTelusCharacteristic(characteristic);
//
//        TelusCreditDecisionWarning warning = new TelusCreditDecisionWarning();
//        warning.setWarningCategoryCd(enc + "setWarningCategoryCd");
//        warning.setWarningCd(enc + "setWarningCd");
//        warning.setWarningTypeCd(enc + "setWarningTypeCd");
//        warning.setWarningItemTypeCd(enc + "setWarningItemTypeCd");
//        warning.setWarningStatusCd(enc + "setWarningStatusCd");
//
//        characteristic.setWarningHistoryList(Collections.singletonList(warning));
//
//        Customer customer = new Customer();
//        customer.setCreditProfile(Collections.singletonList(cp));
//
//        underTest.resolveMissingFields(customer, null);
//        TestUtils.compareObject(customer, "customerEnc.json");
//    }

//    @Test
//    void testDecryptIndividualIdentity() throws Exception {
//        TelusIndividualIdentification identification = new TelusIndividualIdentification();
//        identification.setIdentificationId("abc");
//        Individual individual = new Individual();
//        individual.setIndividualIdentification(Collections.singletonList(identification));
//
//        Customer customer = new Customer();
//
//        String decryptedValue = "cba";
//        doReturn(decryptedValue).when(cryptoService).decryptAndIgnoreError(identification.getIdentificationId());
//
//        underTest.resolveMissingFields(customer, null);
//        assertEquals(decryptedValue, identification.getIdentificationId());
//    }

//    @Test
//    void testDecryptOrganizationIdentity() throws Exception {
//        OrganizationIdentification identification = new OrganizationIdentification();
//        identification.setIdentificationId("abc");
//        Organization organization = new Organization();
//        organization.setOrganizationIdentification(Collections.singletonList(identification));
//
//        Customer customer = new Customer();
//
//        String decryptedValue = "cba";
//
//        underTest.resolveMissingFields(customer, null);
//        assertEquals(decryptedValue, identification.getIdentificationId());
//    }

    @Test
    void testPopulateBureauMsg() {
        Customer test = new Customer();
        List<TelusCreditProfile> cpLst = new ArrayList<>(1);
        TelusCreditProfile cp = new TelusCreditProfile();
        cp.setCreditRiskLevelNum("100");
        TelusCreditProfileCharacteristic telPc = new TelusCreditProfileCharacteristic();
        telPc.setBureauDecisionCode("E29");
        cp.setTelusCharacteristic(telPc);
        cpLst.add(cp);
        test.setCreditProfile(cpLst);

        MultiKeyReferenceDataItem referenceDataItem = new MultiKeyReferenceDataItem();
        List<Value> values = new ArrayList<>();
        Value eng = new Value();
        eng.setLangCode("en");
        eng.setValue("E29 - A 200$ deposit required");
        eng.setValueCode(PdsRefConstants.ENG_MESSAGE);
        values.add(eng);

        Value fr = new Value();
        fr.setLangCode(ApplicationConstants.FR_LANG);
        fr.setValue("E29 - A 200$ deposit required French message");
        fr.setValueCode(PdsRefConstants.FR_MESSAGE);
        values.add(fr);
        referenceDataItem.setValues(values);

        doReturn(referenceDataItem).when(referenceDataService).getCreditDecisionRule(anyList());

        underTest.decryptCustomerFromDb(test);
        assertEquals(fr.getValue(), telPc.getBureauDecisionMessage_fr());
        assertEquals(eng.getValue(), telPc.getBureauDecisionMessage());
    }
    
    @Test
    void testPopulateBureauDecisionMsgWithNoRefpdsData() {

        Customer test = new Customer();
        List<TelusCreditProfile> cpLst = new ArrayList<>(1);
        TelusCreditProfile cp = new TelusCreditProfile();
        cp.setCreditRiskLevelNum("100");
        TelusCreditProfileCharacteristic telPc = new TelusCreditProfileCharacteristic();
        telPc.setBureauDecisionCode("tes123");
        cp.setTelusCharacteristic(telPc);
        cpLst.add(cp);
        test.setCreditProfile(cpLst);

        MultiKeyReferenceDataItem referenceDataItem = new MultiKeyReferenceDataItem();
        List<Value> values = new ArrayList<>();
        Value eng = new Value();
        eng.setLangCode(ApplicationConstants.EN_LANG);
        eng.setValue("tes123");
        eng.setValueCode(PdsRefConstants.ENG_MESSAGE);
        values.add(eng);

        Value fr = new Value();
        fr.setLangCode(ApplicationConstants.FR_LANG);
        fr.setValue("tes123");
        fr.setValueCode(PdsRefConstants.FR_MESSAGE);
        values.add(fr);
        referenceDataItem.setValues(values);

        doReturn(referenceDataItem).when(referenceDataService).getCreditDecisionRule(anyList());

        underTest.decryptCustomerFromDb(test);
        
        assertEquals("tes123", telPc.getBureauDecisionMessage_fr());
        assertEquals("tes123", telPc.getBureauDecisionMessage());
    
    }

    @Test
    void testPopulateAssessmentMsg() {
        Customer test = new Customer();
        List<TelusCreditProfile> cpLst = new ArrayList<>(1);
        TelusCreditProfile cp = new TelusCreditProfile();
        TelusCreditProfileCharacteristic telPc = new TelusCreditProfileCharacteristic();
        telPc.setAssessmentMessageCode("KMSG038");
        cp.setTelusCharacteristic(telPc);
        cpLst.add(cp);
        test.setCreditProfile(cpLst);

        MultiKeyReferenceDataItem referenceDataItem = new MultiKeyReferenceDataItem();
        List<Value> values = new ArrayList<>();
        Value eng = new Value();
        eng.setLangCode(ApplicationConstants.EN_LANG);
        eng.setValue("Approved. Tab Account. Review Bureau Warning");
        eng.setValueCode(PdsRefConstants.ASSESSMENT_MSG_VALUE_CODE);
        values.add(eng);

        Value fr = new Value();
        fr.setLangCode(ApplicationConstants.FR_LANG);
        fr.setValue("La demande est approuvée. Compte avec la Balance. Consultez l’avertissement de l’agence d’évaluation du crédit.");
        fr.setValueCode(PdsRefConstants.ASSESSMENT_MSG_VALUE_CODE);
        values.add(fr);
        referenceDataItem.setValues(values);

        doReturn(referenceDataItem).when(referenceDataService).getAssessmentMessage(anyList());

        underTest.decryptCustomerFromDb(test);
        assertEquals(fr.getValue(), telPc.getAssessmentMessage_fr());
        assertEquals(eng.getValue(), telPc.getAssessmentMessage());
    }
    
    @Test
    void testPopulateAssessmentMsgWithNoRefpdsData() {
        Customer test = new Customer();
        List<TelusCreditProfile> cpLst = new ArrayList<>(1);
        TelusCreditProfile cp = new TelusCreditProfile();
        TelusCreditProfileCharacteristic telPc = new TelusCreditProfileCharacteristic();
        telPc.setAssessmentMessageCode("test");
        cp.setTelusCharacteristic(telPc);
        cpLst.add(cp);
        test.setCreditProfile(cpLst);

        MultiKeyReferenceDataItem referenceDataItem = new MultiKeyReferenceDataItem();
        List<Value> values = new ArrayList<>();
        Value eng = new Value();
        eng.setLangCode(ApplicationConstants.EN_LANG);
        eng.setValue("test");
        eng.setValueCode(PdsRefConstants.ASSESSMENT_MSG_VALUE_CODE);
        values.add(eng);

        Value fr = new Value();
        fr.setLangCode(ApplicationConstants.FR_LANG);
        fr.setValue("test");
        fr.setValueCode(PdsRefConstants.ASSESSMENT_MSG_VALUE_CODE);
        values.add(fr);
        referenceDataItem.setValues(values);

        doReturn(referenceDataItem).when(referenceDataService).getAssessmentMessage(anyList());

        underTest.decryptCustomerFromDb(test);
        
        assertEquals("test", telPc.getAssessmentMessage_fr());
        assertEquals("test", telPc.getAssessmentMessage());
    }
    
}