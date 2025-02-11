/*
 * package com.telus.credit.service.pds;
 * 
 * import java.util.ArrayList; import java.util.List;
 * 
 * import org.junit.jupiter.api.Assertions; import org.junit.jupiter.api.Test;
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.boot.test.context.SpringBootTest; import
 * org.springframework.test.context.event.annotation.BeforeTestExecution;
 * 
 * import com.telus.credit.config.ReferenceDataConfig; import
 * com.telus.credit.pds.cache.MultiKeyCacheKeyGenerator; import
 * com.telus.credit.pds.model.Key; import
 * com.telus.credit.pds.model.MultiKeyReferenceDataItem; import
 * com.telus.credit.pds.model.SingleKeyReferenceDataItem; import
 * com.telus.credit.pds.model.Value; import
 * com.telus.credit.pds.service.MultiKeyReferenceDataService; import
 * com.telus.credit.pds.service.ReferenceDataService; import
 * com.telus.credit.pds.service.SingleKeyReferenceDataService;
 * 
 * @SpringBootTest(classes = { ReferenceDataConfig.class,
 * ReferenceDataService.class, SingleKeyReferenceDataService.class,
 * MultiKeyReferenceDataService.class, MultiKeyCacheKeyGenerator.class}) public
 * class ReferenceDataServiceTest {
 * 
 * @Autowired ReferenceDataService referenceDataService;
 * 
 * @BeforeTestExecution public void initCheck() { Assertions.assertNotNull(
 * referenceDataService, "ReferenceDataService instance is not available."); }
 * 
 * @Test public void contextLoads() {}
 * 
 * @Test public void testSingleKeyData() { SingleKeyReferenceDataItem data =
 * referenceDataService.getAddressType("P", "en", "1"); Value value = new
 * Value(); value.setLangCode("en"); value.setValueCode("1");
 * value.setValue("P.O.B."); Assertions.assertNotNull(data,
 * "AddressType not found for key=P, lang=en, valueCode=1");
 * Assertions.assertFalse( data.getValues().isEmpty(),
 * "AddressType not found for key=P, lang=en, valueCode=1");
 * Assertions.assertTrue( data.getValues().contains(value),
 * "Expected AddressType value not found for key=P, lang=en, valueCode=1"); }
 * 
 * @Test public void testSingleKeyDataNegative() { Assertions.assertThrows(
 * IllegalArgumentException.class, () -> {
 * referenceDataService.getAddressType("K", "en", "1"); }); }
 * 
 * @Test public void testCreditDecisionRule() { List<Key> keys =
 * MultiKeyReferenceDataService.createKeyList("DECISION_CD", "E29");
 * MultiKeyReferenceDataItem data =
 * referenceDataService.getCreditDecisionRule(keys); List<Value> values = new
 * ArrayList<>(); Value v1 = new Value(); v1.setValueCode("CR_REFERAL_IND");
 * v1.setValue("Y"); Value v2 = new Value(); v2.setValueCode("WAIVE_DPST_IND");
 * v2.setValue("Y"); Value v3 = new Value(); v3.setValueCode("ORIG_DPST_IND");
 * v3.setValue("Y"); Value v4 = new Value(); v4.setValueCode("ORIG_DPST_AMT");
 * v4.setValue("200"); values.add(v1); values.add(v2); values.add(v3);
 * values.add(v4); Assertions.assertNotNull(data, "Value not found for key=" +
 * keys.toString()); Assertions.assertFalse( data.getValues().isEmpty(),
 * "Value not found for key=" + keys.toString()); Assertions.assertTrue(
 * data.getValues().containsAll(values), "Value not found for key=" +
 * keys.toString()); }
 * 
 * @Test public void testCreditOperationParameter() { List<Key> keys =
 * MultiKeyReferenceDataService.createKeyList("APPL_ID", "WLSCreditAssessment");
 * keys.add(MultiKeyReferenceDataService.createKey("ENVIR_NM", "IT03"));
 * keys.add(MultiKeyReferenceDataService.createKey("PARM_NM",
 * "WLS_WCDAP_LOGGING_ENABLED")); MultiKeyReferenceDataItem data =
 * referenceDataService.getCreditOperationParameter(keys); List<Value> values =
 * new ArrayList<>(); Value v1 = new Value(); v1.setValueCode("PARM_VALUE_STR");
 * v1.setValue("Y"); v1.setLangCode("en"); values.add(v1);
 * Assertions.assertNotNull(data, "Value not found for key=" + keys.toString());
 * Assertions.assertFalse( data.getValues().isEmpty(),
 * "Value not found for key=" + keys.toString()); Assertions.assertTrue(
 * data.getValues().containsAll(values), "Value not found for key=" +
 * keys.toString()); }
 * 
 * @Test public void testMultiKeyDataNegative() { List<Key> keysDecisionCode =
 * MultiKeyReferenceDataService.createKeyList("DECISION_CD", "tes");
 * MultiKeyReferenceDataItem dataDecisionCode =
 * referenceDataService.getCreditDecisionRule(keysDecisionCode);
 * Assertions.assertNull(dataDecisionCode,
 * "Value in refpds is not found for key=" + keysDecisionCode.toString());
 * 
 * List<Key> keysAssessmentCode =
 * MultiKeyReferenceDataService.createKeyList("MessageKey", "tes");
 * MultiKeyReferenceDataItem dataAssessmentCode =
 * referenceDataService.getCreditDecisionRule(keysAssessmentCode);
 * Assertions.assertNull(dataAssessmentCode,
 * "Value in refpds is not found for key=" + keysAssessmentCode.toString()); }
 * 
 * @Test public void testAssessmentMessage() { List<Key> keys =
 * MultiKeyReferenceDataService.createKeyList("MessageKey", "TMSG080");
 * MultiKeyReferenceDataItem data =
 * referenceDataService.getAssessmentMessage(keys); List<Value> values = new
 * ArrayList<>(); Value v1 = new Value(); v1.setValueCode("4");
 * v1.setValue("Decline Service"); v1.setLangCode("en"); Value v2 = new Value();
 * v2.setValueCode("4"); v2.setValue("Le service a été refusé.");
 * v2.setLangCode("fr"); values.add(v1); values.add(v2);
 * Assertions.assertNotNull(data, "Value not found for key=" + keys.toString());
 * Assertions.assertFalse( data.getValues().isEmpty(),
 * "Value not found for key=" + keys.toString()); Assertions.assertTrue(
 * data.getValues().containsAll(values), "Value not found for key=" +
 * keys.toString()); }
 * 
 * @Test public void testAssessmentMessageFr() { List<Key> keys =
 * MultiKeyReferenceDataService.createKeyList("MessageKey", "TMSG080");
 * MultiKeyReferenceDataItem data =
 * referenceDataService.getAssessmentMessage(keys, "fr"); List<Value> values =
 * new ArrayList<>(); Value v2 = new Value(); v2.setValueCode("4");
 * v2.setValue("Le service a été refusé."); v2.setLangCode("fr");
 * values.add(v2); Assertions.assertNotNull(data, "Value not found for key=" +
 * keys.toString()); Assertions.assertFalse( data.getValues().isEmpty(),
 * "Value not found for key=" + keys.toString()); Assertions.assertTrue(
 * data.getValues().containsAll(values), "Value not found for key=" +
 * keys.toString()); }
 * 
 * @Test public void testAssessmentMessageEn() { List<Key> keys =
 * MultiKeyReferenceDataService.createKeyList("MessageKey", "TMSG080");
 * MultiKeyReferenceDataItem data =
 * referenceDataService.getAssessmentMessage(keys); List<Value> values = new
 * ArrayList<>(); Value v1 = new Value(); v1.setValueCode("4");
 * v1.setValue("Decline Service"); v1.setLangCode("en"); values.add(v1);
 * Assertions.assertNotNull(data, "Value not found for key=" + keys.toString());
 * Assertions.assertFalse( data.getValues().isEmpty(),
 * "Value not found for key=" + keys.toString()); Assertions.assertTrue(
 * data.getValues().containsAll(values), "Value not found for key=" +
 * keys.toString()); } }
 */