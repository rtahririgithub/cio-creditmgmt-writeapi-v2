/*
 * package com.telus.credit.service.impl;
 * 
 * import static org.assertj.core.api.Assertions.assertThatThrownBy; import
 * static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
 * 
 * 
 * import java.io.IOException; import java.util.Arrays; import java.util.List;
 * 
 * import javax.validation.Validation;
 * 
 * import com.telus.credit.model.*; import
 * org.assertj.core.api.AbstractThrowableAssert; import
 * org.assertj.core.api.ThrowableAssert; import
 * org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import
 * org.junit.jupiter.api.extension.ExtendWith; import
 * org.mockito.junit.jupiter.MockitoExtension; import
 * org.springframework.boot.test.context.SpringBootTest; import
 * org.springframework.context.support.ResourceBundleMessageSource;
 * 
 * import com.fasterxml.jackson.databind.ObjectMapper; import
 * com.telus.credit.common.ErrorCode; import com.telus.credit.common.LangHelper;
 * import com.telus.credit.config.ReferenceDataConfig; import
 * com.telus.credit.exceptions.CreditException; import
 * com.telus.credit.model.common.WarningStatusCode; import
 * com.telus.credit.pds.cache.MultiKeyCacheKeyGenerator; import
 * com.telus.credit.pds.service.MultiKeyReferenceDataService; import
 * com.telus.credit.pds.service.ReferenceDataService; import
 * com.telus.credit.pds.service.SingleKeyReferenceDataService; import
 * com.telus.credit.service.ValidationService; import
 * com.telus.credit.validation.CountryCodeValidation; import
 * com.telus.credit.validation.CreditClassValidation; import
 * com.telus.credit.validation.CreditProgramNameValidation; import
 * com.telus.credit.validation.CreditWarningCategoryValidation; import
 * com.telus.credit.validation.DecisionCodeValidation; import
 * com.telus.credit.validation.ProvinceCodeValidation;
 * 
 * @SpringBootTest(classes = { ReferenceDataConfig.class,
 * ReferenceDataService.class, SingleKeyReferenceDataService.class,
 * MultiKeyReferenceDataService.class, MultiKeyCacheKeyGenerator.class,
 * ResourceBundleMessageSource.class, LangHelper.class,
 * CountryCodeValidation.class, ProvinceCodeValidation.class,
 * DecisionCodeValidation.class, CreditClassValidation.class,
 * CreditProgramNameValidation.class, CreditWarningCategoryValidation.class
 * 
 * })
 * 
 * @ExtendWith(MockitoExtension.class) class CustomerValidationTest {
 * 
 * //private static final String TEST_FILE_INDIV =
 * "create-profile-request-individual.json"; private static final String
 * NEW_TEST_FILE="create-new-credit-profile-validation.json"; //private static
 * final String TEST_FILE_ORG = "create-profile-request-organization.json";
 * public static final String INVALID_END_DATE = "2000-01-09T16:51:22.620Z";
 * 
 * private ValidationService underTest; private CreditProfileToCreate
 * creditProfileToCreate=null;
 * 
 * @BeforeEach public void setup() throws IOException { underTest = new
 * ValidationService(Validation.buildDefaultValidatorFactory().getValidator());
 * 
 * try { //load a valid CreditProfile payload. creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class); } catch (IOException e) {
 * e.printStackTrace(); throw e; } }
 * 
 * 
 * @Test void testMissingCreditProfile() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * creditProfileToCreate.setCreditProfile(null); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate), ErrorCode.C_1100,
 * "creditProfile: null"); assertDoesNotThrow(() ->
 * underTest.validateForPatch(creditProfileToCreate));
 * 
 * // creditProfileToCreate.setCreditProfile(Collections.emptyList()); //
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1100, "creditProfile: []"); // assertDoesNotThrow(() ->
 * underTest.validateForPatch(creditProfileToCreate)); // //
 * creditProfileToCreate.setCreditProfile(Collections.singletonList(new
 * TelusCreditProfile())); // assertDoesNotThrow(() ->
 * underTest.validateForPatch(creditProfileToCreate)); }
 * 
 * // @Test // void testNullEngagedParty() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class); // //
 * creditProfileToCreate.setEngagedParty(null); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate), ErrorCode.C_1108,
 * "engagedParty: null"); // assertDoesNotThrow(() ->
 * underTest.validateForPatch(creditProfileToCreate)); // // RelatedPartyToPatch
 * relatedPartyToPatch = new RelatedPartyToPatch(); //
 * relatedPartyToPatch.setAtReferredType(PartyType.INDIVIDUAL.getType()); //
 * creditProfileToCreate.setEngagedParty(relatedPartyToPatch); //
 * assertDoesNotThrow(() -> underTest.validateForPatch(creditProfileToCreate));
 * // }
 * 
 * // @Test // void testInvalidAuditCharacteristic() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class); // //
 * creditProfileToCreate.setTelusAuditCharacteristic(null); //
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1113,"telusAuditCharacteristic: null"); // //
 * creditProfileToCreate.setTelusAuditCharacteristic(null); //
 * assertInvalidInput(() -> underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1113, "telusAuditCharacteristic: null"); // }
 * 
 * @Test void testInvalidAuditCharacteristicOriginatorApplicationId() throws
 * IOException { //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * 
 * creditProfileToCreate.getTelusAuditCharacteristic().
 * setOriginatorApplicationId(null); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1112,"originatorApplicationId: null"); assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate), ErrorCode.C_1112,
 * "originatorApplicationId: null");
 * 
 * creditProfileToCreate.getTelusAuditCharacteristic().
 * setOriginatorApplicationId(""); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1112,"originatorApplicationId: "); assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate), ErrorCode.C_1112,
 * "originatorApplicationId: "); }
 * 
 * @Test void testInvalidAuditCharacteristicUserId() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * 
 * creditProfileToCreate.getTelusAuditCharacteristic().setUserId(null);
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1113,"userId: null"); assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate), ErrorCode.C_1113,
 * "userId: null");
 * 
 * creditProfileToCreate.getTelusAuditCharacteristic().setUserId("");
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1113,"userId: "); assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate), ErrorCode.C_1113,
 * "userId: "); }
 * 
 * // @Test // void testBlankContactMediumType() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class); // //
 * creditProfileToCreate.getEngagedParty().getContactMedium().get(0).
 * setMediumType(null); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1105,"mediumType: null"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate), ErrorCode.C_1105,
 * "mediumType: null"); // //
 * creditProfileToCreate.getEngagedParty().getContactMedium().get(0).
 * setMediumType(""); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1105,"mediumType: "); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate), ErrorCode.C_1105,
 * "mediumType: "); // } // // @Test // void testContactMediumTypeForPatch() {
 * // RelatedPartyToPatch partyToPatch = new RelatedPartyToPatch(); //
 * partyToPatch.setAtReferredType(PartyType.INDIVIDUAL.getType()); // //
 * partyToPatch.setContactMedium(null); // assertDoesNotThrow(() ->
 * underTest.validateForPatch(partyToPatch)); // //
 * partyToPatch.setContactMedium(Collections.emptyList()); //
 * assertDoesNotThrow(() -> underTest.validateForPatch(partyToPatch)); // //
 * partyToPatch.setContactMedium(Collections.singletonList(new
 * ContactMedium())); // assertDoesNotThrow(() ->
 * underTest.validateForPatch(partyToPatch)); // }
 * 
 * // @Test // void testIndividualIdentificationType() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class); // //
 * creditProfileToCreate.getEngagedParty().getIndividualIdentification().get(0).
 * setIdentificationType(""); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: "); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate), ErrorCode.C_1111,
 * "identificationType: "); // //
 * creditProfileToCreate.getEngagedParty().getIndividualIdentification().get(0).
 * setIdentificationType(null); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: null"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: null"); // //
 * creditProfileToCreate.getEngagedParty().getIndividualIdentification().get(0).
 * setIdentificationType("SIN"); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: SIN"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: SIN"); // }
 * 
 * // @Test // void testEmptyIndividualIdentificationTypeForPatch() { //
 * RelatedPartyToPatch partyToPatch = new RelatedPartyToPatch(); //
 * partyToPatch.setAtReferredType(PartyType.INDIVIDUAL.getType()); // //
 * partyToPatch.setIndividualIdentification(null); // assertDoesNotThrow(() ->
 * underTest.validateForPatch(partyToPatch)); // //
 * partyToPatch.setIndividualIdentification(Collections.emptyList()); //
 * assertDoesNotThrow(() -> underTest.validateForPatch(partyToPatch)); // //
 * partyToPatch.setIndividualIdentification(Collections.singletonList(new
 * TelusIndividualIdentification())); // assertInvalidInput(() ->
 * underTest.validateForPatch(partyToPatch), ErrorCode.C_1111,
 * "identificationType: null"); // }
 * 
 * // @Test // void testOrganizationIdentificationType() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_ORG), CustomerToPatch.class); // //
 * creditProfileToCreate.getEngagedParty().getOrganizationIdentification().get(0
 * ).setIdentificationType(""); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: "); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: "); // //
 * creditProfileToCreate.getEngagedParty().getOrganizationIdentification().get(0
 * ).setIdentificationType(null); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: null"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: null"); // //
 * creditProfileToCreate.getEngagedParty().getOrganizationIdentification().get(0
 * ).setIdentificationType("BIC"); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: BIC"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1111,"identificationType: BIC"); // }
 * 
 * // @Test // void testEmptyOrganizationIdentificationTypeForPatch() { //
 * RelatedPartyToPatch partyToPatch = new RelatedPartyToPatch(); //
 * partyToPatch.setAtReferredType(PartyType.INDIVIDUAL.getType()); // //
 * partyToPatch.setOrganizationIdentification(null); // assertDoesNotThrow(() ->
 * underTest.validateForPatch(partyToPatch)); // //
 * partyToPatch.setOrganizationIdentification(Collections.emptyList()); //
 * assertDoesNotThrow(() -> underTest.validateForPatch(partyToPatch)); // //
 * partyToPatch.setOrganizationIdentification(Collections.singletonList(new
 * OrganizationIdentification())); // assertInvalidInput(() ->
 * underTest.validateForPatch(partyToPatch), ErrorCode.C_1111,
 * "identificationType: null"); // }
 * 
 * // @Test // void testContactMediumTimePeriod() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_ORG), CustomerToPatch.class); // //
 * creditProfileToCreate.getEngagedParty().getContactMedium().get(0).getValidFor
 * ().setEndDateTime(INVALID_END_DATE); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1101,"validFor:"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1101,"validFor:"); // }
 * 
 * @Test void testCreditProfileTimePeriod() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * 
 * creditProfileToCreate.getCreditProfile().getValidFor().setEndDateTime(
 * INVALID_END_DATE); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1101,"validFor:"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1101,"validFor:"); }
 * 
 * // @Test // void testOrganizationIdentificationTimePeriod() throws
 * IOException { // CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_ORG), CustomerToPatch.class); // //
 * creditProfileToCreate.getEngagedParty().getOrganizationIdentification().get(0
 * ).getValidFor().setEndDateTime(INVALID_END_DATE); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1101,"validFor:"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1101,"validFor:"); // }
 * 
 * // @Test // void testIndividualIdentificationTimePeriod() throws IOException
 * { // CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class); // //
 * creditProfileToCreate.getEngagedParty().getIndividualIdentification().get(0).
 * getValidFor().setEndDateTime(INVALID_END_DATE); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1101,"validFor:"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1101,"validFor:"); // }
 * 
 * @Test void testCreditDecisionWarningTimePeriod() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * 
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList().get(0).
 * getValidFor().setEndDateTime(INVALID_END_DATE); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1101,"validFor:"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1101,"validFor:"); }
 * 
 * // @Test // void testBirthDateValidation() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class); // //
 * creditProfileToCreate.getEngagedParty().setBirthDate("1800-12-12"); //
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1104,"birthDate: 1800-12-12"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1104,"birthDate: 1800-12-12"); // //
 * creditProfileToCreate.getEngagedParty().setBirthDate("9999-12-12"); //
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1104,"birthDate: 9999-12-12"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1104,"birthDate: 9999-12-12"); // //
 * creditProfileToCreate.getEngagedParty().setBirthDate("1900-01-01"); //
 * assertDoesNotThrow(() -> underTest.validateForCreate(creditProfileToCreate));
 * // assertDoesNotThrow(() ->
 * underTest.validateForPatch(creditProfileToCreate)); // //
 * creditProfileToCreate.getEngagedParty().setBirthDate("2013-12-31"); //
 * assertDoesNotThrow(() -> underTest.validateForCreate(creditProfileToCreate));
 * // assertDoesNotThrow(() ->
 * underTest.validateForPatch(creditProfileToCreate)); // }
 * 
 * 
 * @Test void testContactMediumCountryValidation() throws IOException {
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class);
 * 
 * doThrow(IllegalArgumentException.class).when(referenceDataService).
 * getCountryOverseas(eq("ptcountry"), eq("en"), eq("1"));
 * 
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1109,"country: ptcountry"); assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1109,"country: ptcountry"); }
 * 
 * 
 * @Test void testContactMediumProvinceValidation() throws IOException {
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class);
 * 
 * doThrow(IllegalArgumentException.class).when(referenceDataService).
 * getProvinceState(eq("ptstateOrProvince"), eq("en"), eq("1"));
 * 
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1110,"stateOrProvince: ptstateOrProvince"); assertInvalidInput(()
 * -> underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1110,"stateOrProvince: ptstateOrProvince"); }
 * 
 * 
 * 
 * @Test void testIdentificationCountryValidation() throws IOException {
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class);
 * 
 * doThrow(IllegalArgumentException.class).when(referenceDataService).
 * getCountryOverseas(eq("idcountryCd"), eq("en"), eq("1"));
 * 
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1109,"countryCd: idcountryCd"); assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1109,"countryCd: idcountryCd"); }
 * 
 * 
 * 
 * @Test void testIdentificationProvinceValidation() throws IOException {
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class);
 * 
 * doThrow(IllegalArgumentException.class).when(referenceDataService).
 * getProvinceState(eq("idprovinceCd"), eq("en"), eq("1"));
 * 
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1110,"provinceCd: idprovinceCd"); assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1110,"provinceCd: idprovinceCd"); }
 * 
 * 
 * 
 * @Test void testDecisionCodeValidation() throws IOException { CustomerToPatch
 * creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class);
 * 
 * doThrow(IllegalArgumentException.class).when(referenceDataService).
 * getCreditDecisionRule(anyList());
 * 
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1102,"bureauDecisionCode: bureauDecisionCode");
 * assertInvalidInput(() -> underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1102,"bureauDecisionCode: bureauDecisionCode"); }
 * 
 * 
 * 
 * 
 * The test shall pass if ValidationService throws a CreditException when an
 * invalid credit class is provided.
 * 
 * 
 * @Test void test_invalid_credit_class_validation() throws IOException {
 * 
 * //simulate an invalid credit class value in the payload String
 * invalidCreditClassCd="INVALID";
 * creditProfileToCreate.getCreditProfile().setCreditClassCd(
 * invalidCreditClassCd);
 * 
 * //assert. Expected result= validateForCreate method throws a CreditException
 * assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),ErrorCode.C_1115
 * ,"creditClassCd: "+ invalidCreditClassCd );
 * 
 * //assert. Expected result= validateForPatch method throws a CreditException
 * assertInvalidInput(() -> underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1115,"creditClassCd: "+ invalidCreditClassCd ); }
 * 
 * @Test void testInvalidWarningStatusCode() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * 
 * TelusCreditDecisionWarning warning =
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList().get(0);
 * warning.setWarningStatusCd(WarningStatusCode.INACTIVE.name() + "_invalid");
 * 
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1117,"warningStatusCd: INACTIVE_invalid"); }
 * 
 * @Test void testBlankWarningStatusCode() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * 
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList()
 * .get(0).setWarningStatusCd(null); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1117,"warningStatusCd: null"); assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1117,"warningStatusCd: null");
 * 
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList()
 * .get(0).setWarningStatusCd(""); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1117,"warningStatusCd: "); assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1117,"warningStatusCd: "); }
 * 
 * // @Test // void testBlankRelatedPartyRole() throws IOException { //
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class); // //
 * creditProfileToCreate.getCreditProfile().getRelatedParties() //
 * .get(0).setRole(null); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1117,"role: null"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1117,"role: null"); // //
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList() //
 * .get(0).setWarningStatusCd(""); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1117,"warningStatusCd: "); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1117,"warningStatusCd: "); // }
 * 
 * @Test
 * 
 * void test_invalid_WarningCategoryCode_validation() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * 
 * // TelusCreditDecisionWarning warning =
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList().get(0); //
 * warning.setWarningCategoryCd(null);
 * 
 * // doThrow(IllegalArgumentException.class).when(referenceDataService).
 * getCreditWarningCategory(eq("warningCategoryCd"), eq("en"), eq("1")); String
 * invalidwarningCategoryCd="INVALID_WARNINGCATEGORYCD";
 * List<TelusCreditDecisionWarning> aWarningHistoryList =
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList(); if(
 * aWarningHistoryList!=null && aWarningHistoryList.get(0)!=null) {
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList().get(0).
 * setWarningCategoryCd(invalidwarningCategoryCd);
 * 
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1116, "warningCategoryCd: "+invalidwarningCategoryCd);
 * assertInvalidInput(() -> underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1116,"warningCategoryCd: "+invalidwarningCategoryCd); } }
 * 
 * @Test void testBlankWarningCategoryCode() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * 
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList()
 * .get(0).setWarningCategoryCd(null); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1116,"warningCategoryCd: null");
 * 
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList()
 * .get(0).setWarningCategoryCd(""); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1116,"warningCategoryCd: "); assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1116,"warningCategoryCd: "); }
 * 
 * @Test void testBlankWarningDetectionTs() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * 
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList()
 * .get(0).setWarningDetectionTs(null); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1118,"warningDetectionTs: null");
 * 
 * creditProfileToCreate.getCreditProfile().getWarningHistoryList()
 * .get(0).setWarningDetectionTs(""); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1118,"warningDetectionTs: "); }
 * 
 * @Test void testNullWarningDetectionTsForPatch() { TelusCreditDecisionWarning
 * telusCreditDecisionWarning = new TelusCreditDecisionWarning();
 * assertDoesNotThrow(() ->
 * underTest.validateForPatch(telusCreditDecisionWarning)); }
 * 
 * @Test void testBlankRiskRating() throws IOException { //CreditProfileToCreate
 * creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * 
 * creditProfileToCreate.getCreditProfile().setCreditRiskLevelNum(null);
 * assertDoesNotThrow(() -> underTest.validateForCreate(creditProfileToCreate));
 * 
 * creditProfileToCreate.getCreditProfile().setCreditRiskLevelNum("");
 * assertDoesNotThrow(() -> underTest.validateForCreate(creditProfileToCreate));
 * assertDoesNotThrow(() -> underTest.validateForPatch(creditProfileToCreate));
 * }
 * 
 * @Test void testBlankCreditScore() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * creditProfileToCreate.getCreditProfile().setPrimaryCreditScoreCd(null);
 * assertDoesNotThrow(() -> underTest.validateForCreate(creditProfileToCreate));
 * 
 * creditProfileToCreate.getCreditProfile().setPrimaryCreditScoreCd("");
 * assertDoesNotThrow(() -> underTest.validateForCreate(creditProfileToCreate));
 * }
 * 
 * 
 * 
 * @Test void testValidRiskRatingNumber() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * creditProfileToCreate.getCreditProfile().setCreditRiskLevelNum("-1");
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1124,"creditRiskLevelNum: -1");
 * 
 * creditProfileToCreate.getCreditProfile().setCreditRiskLevelNum("0");
 * assertDoesNotThrow(() -> underTest.validateForCreate(creditProfileToCreate));
 * 
 * }
 * 
 * @Test void testValidPrimaryCreditScoreCd() throws IOException {
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class);
 * //creditProfileToCreate.getCreditProfile().setPrimaryCreditScoreCd("abc");
 * //assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * null,"primaryCreditScoreCd: abc"); }
 * 
 * @Test
 * 
 * void test_invalid_CreditProgramName_validation() throws IOException {
 * 
 * //CreditProfileToCreate creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (NEW_TEST_FILE), CreditProfileToCreate.class); //
 * doThrow(IllegalArgumentException.class).when(referenceDataService).
 * getCreditProgramName(eq("string"), eq("en"), eq("1")); String
 * invalidCreditProgramName="INVALID_CREDITPROGRAM";
 * creditProfileToCreate.getCreditProfile().setCreditProgramName(
 * invalidCreditProgramName); assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1114,"creditProgramName: "+invalidCreditProgramName); }
 * 
 * // @Test // void testReferredType() throws IOException { // CustomerToPatch
 * creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class); // //
 * creditProfileToCreate.getEngagedParty().setAtReferredType(null); //
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1103,"atReferredType: null"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1103,"atReferredType: null"); // //
 * creditProfileToCreate.getEngagedParty().setAtReferredType(""); //
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1103,"atReferredType: "); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1103,"atReferredType: "); // //
 * creditProfileToCreate.getEngagedParty().setAtReferredType("invalid"); //
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1103,"atReferredType: invalid"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1103,"atReferredType: invalid"); // }
 * 
 * // @Test // void testPartyIdentification() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class); // // OrganizationIdentification
 * organizationIdentification = new OrganizationIdentification(); //
 * organizationIdentification.setIdentificationType("Drivers License"); // //
 * creditProfileToCreate.getEngagedParty().setOrganizationIdentification(
 * Collections.singletonList(organizationIdentification)); //
 * assertInvalidInput(() -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1106,"engagedParty: [trim]"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1106,"engagedParty: [trim]"); // } // // @Test // void
 * testDuplicateIndividualIdentification() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class); // //
 * List<TelusIndividualIdentification> identifications =
 * creditProfileToCreate.getEngagedParty().getIndividualIdentification(); //
 * TelusIndividualIdentification identification = new
 * TelusIndividualIdentification(); // identifications.add(identification); //
 * // identification.setIdentificationType(identifications.get(0).
 * getIdentificationType()); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1107,"individualIdentification: [trim]"); //
 * assertInvalidInput(() -> underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1107,"individualIdentification: [trim]"); // } // // @Test //
 * void testDuplicateOrganizationIdentification() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_ORG), CustomerToPatch.class); // //
 * List<OrganizationIdentification> identifications =
 * creditProfileToCreate.getEngagedParty().getOrganizationIdentification(); //
 * OrganizationIdentification identification = new OrganizationIdentification();
 * // identifications.add(identification); // //
 * identification.setIdentificationType(identifications.get(0).
 * getIdentificationType()); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1107,"organizationIdentification: [trim]"); //
 * assertInvalidInput(() -> underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1107,"organizationIdentification: [trim]"); // } // // @Test //
 * void testMissingIndividualIdentificationId() throws IOException { //
 * CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_INDIV), CustomerToPatch.class); // //
 * List<TelusIndividualIdentification> identifications =
 * creditProfileToCreate.getEngagedParty().getIndividualIdentification(); // //
 * identifications.get(0).setIdentificationId(null); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1123,"identificationId: null"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1123,"identificationId: null"); // //
 * identifications.get(0).setIdentificationId(""); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1123,"identificationId: "); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1123,"identificationId: "); // // TelusIndividualIdentification
 * identificationPatch = new TelusIndividualIdentification(); //
 * identificationPatch.setIdentificationType(IdentificationType.CC.getDesc());
 * // assertDoesNotThrow(() -> underTest.validateForPatch(identificationPatch));
 * // }
 * 
 * // @Test // void testMissingOrganizationIdentification() throws IOException {
 * // CustomerToPatch creditProfileToCreate = new
 * ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream
 * (TEST_FILE_ORG), CustomerToPatch.class); // //
 * List<OrganizationIdentification> identifications =
 * creditProfileToCreate.getEngagedParty().getOrganizationIdentification(); //
 * // identifications.get(0).setIdentificationId(null); // assertInvalidInput(()
 * -> underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1123,"identificationId: null"); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1123,"identificationId: null"); // //
 * identifications.get(0).setIdentificationId(""); // assertInvalidInput(() ->
 * underTest.validateForCreate(creditProfileToCreate),
 * ErrorCode.C_1123,"identificationId: "); // assertInvalidInput(() ->
 * underTest.validateForPatch(creditProfileToCreate),
 * ErrorCode.C_1123,"identificationId: "); // // TelusIndividualIdentification
 * identificationPatch = new TelusIndividualIdentification(); //
 * identificationPatch.setIdentificationType(IdentificationType.CC.getDesc());
 * // assertDoesNotThrow(() -> underTest.validateForPatch(identificationPatch));
 * // }
 * 
 * private void assertInvalidInput(ThrowableAssert.ThrowingCallable callable,
 * ErrorCode code, String message) { AbstractThrowableAssert<?, ? extends
 * Throwable> assertion = assertThatThrownBy(callable)
 * .isInstanceOf(CreditException.class) .hasMessageContaining(message);
 * 
 * if (code != null) { assertion.extracting(e ->
 * Arrays.asList(((CreditException) e).getCode(), ((CreditException)
 * e).getReason())) .isEqualTo(Arrays.asList(code.code(), code.getMessage())); }
 * } }
 */