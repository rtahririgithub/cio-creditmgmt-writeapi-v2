//package com.telus.credit.service.impl;
//
//import static com.telus.credit.model.mapper.IdentificationModelMapper.IDENTIFICATION_ID;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.anyString;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.Optional;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.support.ResourceBundleMessageSource;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.telus.credit.common.LangHelper;
//import com.telus.credit.crypto.service.CryptoService;
//import com.telus.credit.crypto.service.HashService;
//import com.telus.credit.dao.IdentificationAttributesDao;
//import com.telus.credit.dao.IdentificationCharDao;
//import com.telus.credit.dao.IdentificationCharHashDao;
//import com.telus.credit.dao.IndividualDao;
//import com.telus.credit.dao.OrganizationDao;
//import com.telus.credit.dao.PartyContactMediumDao;
//import com.telus.credit.dao.PartyDao;
//import com.telus.credit.dao.PartyIdentificationDao;
//import com.telus.credit.dao.entity.IdentificationAttributesEntity;
//import com.telus.credit.dao.entity.IdentificationCharEntity;
//import com.telus.credit.dao.entity.IdentificationCharHashEntity;
//import com.telus.credit.dao.entity.IndividualEntity;
//import com.telus.credit.dao.entity.OrganizationEntity;
//import com.telus.credit.dao.entity.PartyContactMediumEntity;
//import com.telus.credit.dao.entity.PartyEntity;
//import com.telus.credit.dao.entity.PartyIdentificationEntity;
//import com.telus.credit.dao.entity.PartyIdentificationExEntity;
//import com.telus.credit.exceptions.CreditException;
//import com.telus.credit.model.CustomerToPatch;
//import com.telus.credit.model.RelatedPartyToPatch;
//import com.telus.credit.model.common.IdentificationType;
//import com.telus.credit.model.common.PartyType;
//import com.telus.credit.model.mapper.IdentificationCharacteristicMapper;
//import com.telus.credit.service.ValidationService;
//import com.telus.credit.utils.TestUtils;
//
//@ExtendWith(MockitoExtension.class)
//class DefaultEngagedPartyServiceTest {
//
//    private static final String TEST_FILE_INDIVIDU = "create-profile-request-individual.json";
//
//    private static final String TEST_FILE_ORG = "create-profile-request-organization.json";
//
//    private static final String PARTY_UUID = "17ad1c92-7657-4ccd-ad88-a1996d80a4c1";
//    private static final String PARTY_ID_UUID = "17ad1c92-1657-4ccd-ad88-a1996d80a4c1";
//    private static final String CONTACT_UUID = "944b13b6-b60b-4d08-91bd-90327b68ab02";
//    private static final String IDENTIFICATION_UUID = "144b13b6-b60b-4d08-91bd-90327b68ab02";
//
//    private static final String UPDATE_MAP = "updateMap";
//
//    @Mock
//    private ValidationService validationService;
//
//    @Mock(lenient = true)
//    private CryptoService cryptoService;
//
//    @Mock(lenient = true)
//    private HashService hashService;
//
//    @Mock
//    private PartyDao partyDao;
//
//    @Mock
//    private IndividualDao individualDao;
//
//    @Mock
//    private OrganizationDao organizationDao;
//
//    @Mock
//    private PartyContactMediumDao partyContactMediumDao;
//
//    @Mock
//    private PartyIdentificationDao partyIdentificationDao;
//
//    @Mock
//    private IdentificationCharDao identificationCharDao;
//
//    @Mock
//    private IdentificationCharHashDao identificationCharHashDao;
//
//    @Mock
//    private IdentificationAttributesDao identificationAttributesDao;
//
//    @InjectMocks
//    private PartyIdentificationService partyIdentificationService;
//
//    private DefaultEngagedPartyService underTest;
//
//    @BeforeEach
//    void setup() throws Exception {
//        new LangHelper(new ResourceBundleMessageSource());
//
//        IdentificationCharacteristicMapper identificationCharacteristicMapper = new IdentificationCharacteristicMapper();
//        identificationCharacteristicMapper.setCryptoService(cryptoService);
//        identificationCharacteristicMapper.setHashService(hashService);
//
//        underTest = new DefaultEngagedPartyService(partyDao, partyContactMediumDao, partyIdentificationService,
//                identificationAttributesDao, individualDao, organizationDao, validationService);
//
//        doReturn("ENCRYPTED").when(cryptoService).encrypt(anyString());
//        doReturn("HASHED").when(hashService).sha512CaseInsensitive(anyString());
//    }
//
//    @Test
//    void testCreateIndividual() throws IOException {
//        CustomerToPatch customer = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_FILE_INDIVIDU), CustomerToPatch.class);
//
//        doReturn(Collections.singletonList(new IdentificationAttributesEntity().attributeName(IDENTIFICATION_ID).isEncrypted(true)))
//                .when(identificationAttributesDao).selectAll();
//        doReturn(PARTY_UUID).when(partyDao).insert(any(PartyEntity.class));
//        doReturn(PARTY_ID_UUID).when(partyIdentificationDao).insert(any(PartyIdentificationEntity.class));
//
//        underTest.createEngagedParty(customer.getEngagedParty(), customer.getTelusAuditCharacteristic());
//
//        ArgumentCaptor<PartyEntity> partyEntityCaptor = ArgumentCaptor.forClass(PartyEntity.class);
//        verify(partyDao, times(1)).insert(partyEntityCaptor.capture());
//        TestUtils.compareObject(partyEntityCaptor.getValue(), "entities-create/party-entity.json", UPDATE_MAP);
//
//        ArgumentCaptor<PartyContactMediumEntity> contactMediumEntityCaptor = ArgumentCaptor.forClass(PartyContactMediumEntity.class);
//        verify(partyContactMediumDao, times(1)).insert(contactMediumEntityCaptor.capture());
//        TestUtils.compareObject(contactMediumEntityCaptor.getValue(), "entities-create/contactmediumentity.json", UPDATE_MAP);
//
//        ArgumentCaptor<IndividualEntity> individualEntityCaptor = ArgumentCaptor.forClass(IndividualEntity.class);
//        verify(individualDao, times(1)).insert(individualEntityCaptor.capture());
//        TestUtils.compareObject(individualEntityCaptor.getValue(), "entities-create/individualentity.json", UPDATE_MAP);
//
//        ArgumentCaptor<PartyIdentificationExEntity> partiIdenCaptor = ArgumentCaptor.forClass(PartyIdentificationExEntity.class);
//        verify(partyIdentificationDao, times(1)).insert(partiIdenCaptor.capture());
//        TestUtils.compareObject(partiIdenCaptor.getValue(), "entities-create/partyidentificationentity.json", UPDATE_MAP);
//
//        ArgumentCaptor<IdentificationCharEntity> idenCharacCaptor = ArgumentCaptor.forClass(IdentificationCharEntity.class);
//        verify(identificationCharDao, times(3)).insert(idenCharacCaptor.capture());
//        assertThat(idenCharacCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getCharacteristics());
//
//        ArgumentCaptor<IdentificationCharHashEntity> idenCharacHashCaptor = ArgumentCaptor.forClass(IdentificationCharHashEntity.class);
//        verify(identificationCharHashDao, times(1)).insert(idenCharacHashCaptor.capture());
//        assertThat(idenCharacHashCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getHashedCharacteristics());
//   }
//
//    @Test
//    void testPatchIndividual() throws IOException {
//        CustomerToPatch customer = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream("patch-engagedParty-individual.json"), CustomerToPatch.class);
//        RelatedPartyToPatch engagedParty = customer.getEngagedParty();
//
//        doReturn(Collections.singletonList(new IdentificationAttributesEntity().attributeName(IDENTIFICATION_ID).isEncrypted(true)))
//                .when(identificationAttributesDao).selectAll();
//
//        doReturn(Optional.of(new PartyEntity().partyId(PARTY_UUID).partyType(PartyType.INDIVIDUAL.getType())))
//                .when(partyDao).getById(eq(PARTY_UUID));
//        doReturn(Collections.singletonList(new IndividualEntity().version(1))).when(individualDao).getByPartyIds(anyList());
//
//        doReturn(Optional.of(new PartyContactMediumEntity().partyId(PARTY_UUID).version(1))).when(partyContactMediumDao).getById(CONTACT_UUID);
//
//        doReturn(Optional.of(new PartyIdentificationExEntity().partyId(PARTY_UUID).version(1).identificatonId(IDENTIFICATION_UUID)))
//                .when(partyIdentificationDao).getByPartyIdAndIdType(PARTY_UUID, IdentificationType.PSP.name());
//        doReturn(Optional.empty()).when(partyIdentificationDao).getByPartyIdAndIdType(PARTY_UUID, IdentificationType.DL.name());
//        doReturn(PARTY_ID_UUID).when(partyIdentificationDao).insert(any(PartyIdentificationEntity.class));
//
//        doReturn(Optional.of(new IdentificationCharEntity().identificatonId(IDENTIFICATION_UUID).version(1).identificationCharId("charId")))
//                .when(identificationCharDao).getByIdentificationIdAndKey(eq(IDENTIFICATION_UUID), anyString());
//        doReturn(Optional.empty()).when(identificationCharDao).getByIdentificationIdAndKey(eq(IDENTIFICATION_UUID), eq("countryCd"));
//        doReturn(Optional.of(new IdentificationCharHashEntity().identificatonId(IDENTIFICATION_UUID).version(1).identificationHashId("charHashId")))
//                .when(identificationCharHashDao).getByIdentificationIdAndKey(eq(IDENTIFICATION_UUID), anyString());
//
//        doReturn(1).when(individualDao).update(anyString(), any(IndividualEntity.class));
//        doReturn(1).when(partyContactMediumDao).update(anyString(), any(PartyContactMediumEntity.class));
//        doReturn(1).when(partyIdentificationDao).update(anyString(), any(PartyIdentificationEntity.class));
//        doReturn(1).when(identificationCharDao).update(anyString(), any(IdentificationCharEntity.class));
//        doReturn(1).when(identificationCharHashDao).update(anyString(), any(IdentificationCharHashEntity.class));
//
//        underTest.patchEngagedParty(PARTY_UUID, engagedParty, customer.getTelusAuditCharacteristic());
//
//        verify(validationService, times(1)).validateForPatch(engagedParty);
//
//        verify(validationService, times(1)).validateForCreate(engagedParty.getContactMedium().get(1));
//        ArgumentCaptor<PartyContactMediumEntity> contactMediumEntityCaptor = ArgumentCaptor.forClass(PartyContactMediumEntity.class);
//        verify(partyContactMediumDao, times(1)).insert(contactMediumEntityCaptor.capture());
//        TestUtils.compareObject(contactMediumEntityCaptor.getValue(), "entities-create/contactmediumentity.json", UPDATE_MAP);
//        verify(partyContactMediumDao, times(1)).update(eq(CONTACT_UUID), contactMediumEntityCaptor.capture());
//        TestUtils.compareObject(contactMediumEntityCaptor.getValue(), "entities-patch/contactmediumentity.json", UPDATE_MAP);
//
//        ArgumentCaptor<IndividualEntity> partyEntityCaptor = ArgumentCaptor.forClass(IndividualEntity.class);
//        verify(individualDao, times(1)).update(eq(PARTY_UUID), partyEntityCaptor.capture());
//        TestUtils.compareObject(partyEntityCaptor.getValue(), "entities-patch/individualentity.json", UPDATE_MAP);
//
//        verify(validationService, times(1)).validateForCreate(engagedParty.getIndividualIdentification().get(0));
//        ArgumentCaptor<PartyIdentificationExEntity> partiIdenCaptor = ArgumentCaptor.forClass(PartyIdentificationExEntity.class);
//        verify(partyIdentificationDao, times(1)).insert(partiIdenCaptor.capture());
//        TestUtils.compareObject(partiIdenCaptor.getValue(), "entities-create/partyidentificationentity.json", UPDATE_MAP);
//
//        ArgumentCaptor<IdentificationCharEntity> idenCharacCaptor = ArgumentCaptor.forClass(IdentificationCharEntity.class);
//        verify(identificationCharDao, times(4)).insert(idenCharacCaptor.capture());
//        assertThat(idenCharacCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getCharacteristics());
//
//        ArgumentCaptor<IdentificationCharHashEntity> idenCharacHashCaptor = ArgumentCaptor.forClass(IdentificationCharHashEntity.class);
//        verify(identificationCharHashDao, times(1)).insert(idenCharacHashCaptor.capture());
//        assertThat(idenCharacHashCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getHashedCharacteristics());
//
//        // Verify updates
//
//        partiIdenCaptor = ArgumentCaptor.forClass(PartyIdentificationExEntity.class);
//        verify(partyIdentificationDao, times(1)).update(eq(IDENTIFICATION_UUID), partiIdenCaptor.capture());
//        TestUtils.compareObject(partiIdenCaptor.getValue(), "entities-patch/partyidentificationentity.json", UPDATE_MAP);
//
//        idenCharacCaptor = ArgumentCaptor.forClass(IdentificationCharEntity.class);
//        verify(identificationCharDao, times(2)).update(anyString(), idenCharacCaptor.capture());
//        assertThat(partiIdenCaptor.getValue().getCharacteristics()).containsAll(idenCharacCaptor.getAllValues());
//
//        idenCharacHashCaptor = ArgumentCaptor.forClass(IdentificationCharHashEntity.class);
//        verify(identificationCharHashDao, times(1)).update(anyString(), idenCharacHashCaptor.capture());
//        assertThat(idenCharacHashCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getHashedCharacteristics());
//    }
//
//    @Test
//    void testPatchIndividual_ContactNotFound() throws IOException {
//        CustomerToPatch customer = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream("patch-engagedParty-individual.json"), CustomerToPatch.class);
//
//        doReturn(Optional.empty()).when(partyContactMediumDao).getById(CONTACT_UUID);
//
//        doReturn(Optional.of(new PartyEntity().partyId(PARTY_UUID).partyType(PartyType.INDIVIDUAL.getType())))
//                .when(partyDao).getById(eq(PARTY_UUID));
//        doReturn(Collections.singletonList(new IndividualEntity().version(1))).when(individualDao).getByPartyIds(anyList());
//        doReturn(1).when(individualDao).update(anyString(), any(IndividualEntity.class));
//
//        assertThatThrownBy(() -> underTest.patchEngagedParty(PARTY_UUID, customer.getEngagedParty(), customer.getTelusAuditCharacteristic()))
//                .isInstanceOf(CreditException.class)
//                .hasMessageContaining("Contact medium").hasMessageContaining("not found or");
//
//        doReturn(Optional.of(new PartyContactMediumEntity().partyId(""))).when(partyContactMediumDao).getById(CONTACT_UUID);
//        assertThatThrownBy(() -> underTest.patchEngagedParty(PARTY_UUID, customer.getEngagedParty(), customer.getTelusAuditCharacteristic()))
//                .isInstanceOf(CreditException.class)
//                .hasMessageContaining("Contact medium").hasMessageContaining("not found or");
//    }
//
//    @Test
//    void testCreateOrganization() throws IOException {
//        CustomerToPatch customer = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_FILE_ORG), CustomerToPatch.class);
//
//        doReturn(Collections.singletonList(new IdentificationAttributesEntity().attributeName(IDENTIFICATION_ID).isEncrypted(true)))
//                .when(identificationAttributesDao).selectAll();
//        doReturn(PARTY_UUID).when(partyDao).insert(any(PartyEntity.class));
//        doReturn(PARTY_ID_UUID).when(partyIdentificationDao).insert(any(PartyIdentificationEntity.class));
//
//        underTest.createEngagedParty(customer.getEngagedParty(), customer.getTelusAuditCharacteristic());
//
//        ArgumentCaptor<PartyEntity> partyEntityCaptor = ArgumentCaptor.forClass(PartyEntity.class);
//        verify(partyDao, times(1)).insert(partyEntityCaptor.capture());
//        TestUtils.compareObject(partyEntityCaptor.getValue(), "entities-create/party-org-entity.json", UPDATE_MAP);
//
//        ArgumentCaptor<PartyContactMediumEntity> contactMediumEntityCaptor = ArgumentCaptor.forClass(PartyContactMediumEntity.class);
//        verify(partyContactMediumDao, times(1)).insert(contactMediumEntityCaptor.capture());
//        TestUtils.compareObject(contactMediumEntityCaptor.getValue(), "entities-create/contactmediumentity.json", UPDATE_MAP);
//
//        ArgumentCaptor<OrganizationEntity> individualEntityCaptor = ArgumentCaptor.forClass(OrganizationEntity.class);
//        verify(organizationDao, times(1)).insert(individualEntityCaptor.capture());
//        TestUtils.compareObject(individualEntityCaptor.getValue(), "entities-create/organizationentity.json", UPDATE_MAP);
//
//        ArgumentCaptor<PartyIdentificationExEntity> partiIdenCaptor = ArgumentCaptor.forClass(PartyIdentificationExEntity.class);
//        verify(partyIdentificationDao, times(1)).insert(partiIdenCaptor.capture());
//        TestUtils.compareObject(partiIdenCaptor.getValue(), "entities-create/partyorgidentificationentity.json", UPDATE_MAP);
//
//        ArgumentCaptor<IdentificationCharEntity> idenCharacCaptor = ArgumentCaptor.forClass(IdentificationCharEntity.class);
//        verify(identificationCharDao, times(3)).insert(idenCharacCaptor.capture());
//        assertThat(idenCharacCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getCharacteristics());
//
//        ArgumentCaptor<IdentificationCharHashEntity> idenCharacHashCaptor = ArgumentCaptor.forClass(IdentificationCharHashEntity.class);
//        verify(identificationCharHashDao, times(1)).insert(idenCharacHashCaptor.capture());
//        assertThat(idenCharacHashCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getHashedCharacteristics());
//    }
//
//    @Test
//    void testPatchOrganization() throws IOException {
//        CustomerToPatch customer = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream("patch-engagedParty-organization.json"), CustomerToPatch.class);
//        RelatedPartyToPatch engagedParty = customer.getEngagedParty();
//
//        doReturn(Collections.singletonList(new IdentificationAttributesEntity().attributeName(IDENTIFICATION_ID).isEncrypted(true)))
//                .when(identificationAttributesDao).selectAll();
//
//        doReturn(Optional.of(new PartyEntity().partyId(PARTY_UUID).partyType(PartyType.ORGANIZATION.getType())))
//                .when(partyDao).getById(eq(PARTY_UUID));
//
//        doReturn(Optional.of(new PartyContactMediumEntity().partyId(PARTY_UUID).version(1))).when(partyContactMediumDao).getById(CONTACT_UUID);
//
//        doReturn(Optional.of(new PartyIdentificationExEntity().partyId(PARTY_UUID).version(1).identificatonId(IDENTIFICATION_UUID)))
//                .when(partyIdentificationDao).getByPartyIdAndIdType(PARTY_UUID, IdentificationType.PSP.name());
//        doReturn(Optional.empty()).when(partyIdentificationDao).getByPartyIdAndIdType(PARTY_UUID, IdentificationType.QST.name());
//        doReturn(PARTY_ID_UUID).when(partyIdentificationDao).insert(any(PartyIdentificationEntity.class));
//
//        doReturn(Optional.of(new IdentificationCharEntity().identificatonId(IDENTIFICATION_UUID).version(1).identificationCharId("charId")))
//                .when(identificationCharDao).getByIdentificationIdAndKey(eq(IDENTIFICATION_UUID), anyString());
//        doReturn(Optional.of(new IdentificationCharHashEntity().identificatonId(IDENTIFICATION_UUID).version(1).identificationHashId("charHashId")))
//                .when(identificationCharHashDao).getByIdentificationIdAndKey(eq(IDENTIFICATION_UUID), anyString());
//
//        doReturn(1).when(partyContactMediumDao).update(anyString(), any(PartyContactMediumEntity.class));
//        doReturn(1).when(partyIdentificationDao).update(anyString(), any(PartyIdentificationEntity.class));
//        doReturn(1).when(identificationCharDao).update(anyString(), any(IdentificationCharEntity.class));
//        doReturn(1).when(identificationCharHashDao).update(anyString(), any(IdentificationCharHashEntity.class));
//
//        underTest.patchEngagedParty(PARTY_UUID, engagedParty, customer.getTelusAuditCharacteristic());
//
//        verify(validationService, times(1)).validateForPatch(engagedParty);
//
//        verify(validationService, times(1)).validateForCreate(engagedParty.getContactMedium().get(1));
//        ArgumentCaptor<PartyContactMediumEntity> contactMediumEntityCaptor = ArgumentCaptor.forClass(PartyContactMediumEntity.class);
//        verify(partyContactMediumDao, times(1)).insert(contactMediumEntityCaptor.capture());
//        TestUtils.compareObject(contactMediumEntityCaptor.getValue(), "entities-create/contactmediumentity.json", UPDATE_MAP);
//        verify(partyContactMediumDao, times(1)).update(eq(CONTACT_UUID), contactMediumEntityCaptor.capture());
//        TestUtils.compareObject(contactMediumEntityCaptor.getValue(), "entities-patch/contactmediumentity.json", UPDATE_MAP);
//
//        verify(validationService, times(1)).validateForCreate(engagedParty.getOrganizationIdentification().get(0));
//        ArgumentCaptor<PartyIdentificationExEntity> partiIdenCaptor = ArgumentCaptor.forClass(PartyIdentificationExEntity.class);
//        verify(partyIdentificationDao, times(1)).insert(partiIdenCaptor.capture());
//        TestUtils.compareObject(partiIdenCaptor.getValue(), "entities-create/partyorgidentificationentity.json", UPDATE_MAP);
//
//        ArgumentCaptor<IdentificationCharEntity> idenCharacCaptor = ArgumentCaptor.forClass(IdentificationCharEntity.class);
//        verify(identificationCharDao, times(3)).insert(idenCharacCaptor.capture());
//        assertThat(idenCharacCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getCharacteristics());
//
//        ArgumentCaptor<IdentificationCharHashEntity> idenCharacHashCaptor = ArgumentCaptor.forClass(IdentificationCharHashEntity.class);
//        verify(identificationCharHashDao, times(1)).insert(idenCharacHashCaptor.capture());
//        assertThat(idenCharacHashCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getHashedCharacteristics());
//
//        // Verify updates
//
//        partiIdenCaptor = ArgumentCaptor.forClass(PartyIdentificationExEntity.class);
//        verify(partyIdentificationDao, times(1)).update(eq(IDENTIFICATION_UUID), partiIdenCaptor.capture());
//        TestUtils.compareObject(partiIdenCaptor.getValue(), "entities-patch/partyorgidentificationentity.json", UPDATE_MAP);
//
//        idenCharacCaptor = ArgumentCaptor.forClass(IdentificationCharEntity.class);
//        verify(identificationCharDao, times(3)).update(anyString(), idenCharacCaptor.capture());
//        assertThat(idenCharacCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getCharacteristics());
//
//        idenCharacHashCaptor = ArgumentCaptor.forClass(IdentificationCharHashEntity.class);
//        verify(identificationCharHashDao, times(1)).update(anyString(), idenCharacHashCaptor.capture());
//        assertThat(idenCharacHashCaptor.getAllValues()).containsAll(partiIdenCaptor.getValue().getHashedCharacteristics());
//    }
//}