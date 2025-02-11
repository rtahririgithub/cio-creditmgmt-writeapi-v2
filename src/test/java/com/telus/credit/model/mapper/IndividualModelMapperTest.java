package com.telus.credit.model.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.dao.entity.IndividualEntity;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCreditProfile;

import io.micrometer.core.instrument.util.IOUtils;

@ExtendWith(MockitoExtension.class)
public class IndividualModelMapperTest {
    private static final String LEGAL_CARE_CD = "false";
    private static final String EMPLOYMENT_STATUS_CD_VALUE = "TestStatusCode";
    private static final String PRIM_CRED_CARD_TYP_CD_VALUE = "TestCardTypeCode";
    private static final String RESIDENCY_CD_VALUE = "tx";
    private static final String SEC_CRED_CARD_ISS_CO_TYP_CD_VALUE = "TestCoTypeCode";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @InjectMocks
    private IndividualModelMapper individualModelMapper;

    @Test
    public void testToEntity_withCharacteristics_individual() throws JsonProcessingException {
        TelusCreditProfile creditProfile = getTelusCreditProfile("credit-profile-with-characteristics-at-party-level-individual.json");
        String partyId = "112";
        RelatedPartyToPatch engagedParty = creditProfile.getRelatedParties().get(0).getEngagedParty();
        TelusChannel auditCharacteristic = creditProfile.getChannel();
        IndividualEntity individualEntity = IndividualModelMapper.toEntity(partyId, engagedParty, auditCharacteristic);
        assertThat(individualEntity).isNotNull();
        assertThat(individualEntity.getEmploymentStatusCd()).isEqualTo(EMPLOYMENT_STATUS_CD_VALUE);
        assertThat(individualEntity.getPrimCredCardTypCd()).isEqualTo(PRIM_CRED_CARD_TYP_CD_VALUE);
        assertThat(individualEntity.getResidencyCd()).isEqualTo(RESIDENCY_CD_VALUE);
        assertThat(individualEntity.getSecCredCardIssCoTypCd()).isEqualTo(SEC_CRED_CARD_ISS_CO_TYP_CD_VALUE);
        assertThat(individualEntity.getLegalCareCd()).isEqualTo(LEGAL_CARE_CD);
    }

    @Test
    public void testToEntity_withoutCharacteristics_individual() throws JsonProcessingException {
        TelusCreditProfile creditProfile = getTelusCreditProfile("credit-profile-without-characteristics.json");
        String partyId = "112";
        RelatedPartyToPatch engagedParty = creditProfile.getRelatedParties().get(0).getEngagedParty();
        TelusChannel auditCharacteristic = creditProfile.getChannel();
        IndividualEntity individualEntity = IndividualModelMapper.toEntity(partyId, engagedParty, auditCharacteristic);
        assertThat(individualEntity).isNotNull();
        assertThat(individualEntity.getEmploymentStatusCd()).isNull();
        assertThat(individualEntity.getPrimCredCardTypCd()).isNull();
        assertThat(individualEntity.getResidencyCd()).isNull();
        assertThat(individualEntity.getSecCredCardIssCoTypCd()).isNull();
        assertThat(individualEntity.getLegalCareCd()).isNull();
    }

    private TelusCreditProfile getTelusCreditProfile(String file) throws JsonProcessingException {
        String payload = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8);
        return MAPPER.readValue(payload, TelusCreditProfile.class);
    }

}
