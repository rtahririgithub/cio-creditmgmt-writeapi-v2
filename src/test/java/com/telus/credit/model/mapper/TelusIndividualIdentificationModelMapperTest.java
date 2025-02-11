package com.telus.credit.model.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.telus.credit.dao.entity.IndividualEntity;
import com.telus.credit.dao.entity.PartyEntity;
import com.telus.credit.dao.entity.PartyIdentificationExEntity;
import com.telus.credit.model.Individual;
import com.telus.credit.model.TelusCharacteristic;

@ExtendWith(MockitoExtension.class)
public class TelusIndividualIdentificationModelMapperTest {
    private static final String LEGAL_CARE_CD = "LEGAL_CARE_CD";
    private static final String EMPLOYMENT_STATUS_CD = "EMPLOYMENT_STATUS_CD";
    private static final String PRIM_CRED_CARD_TYP_CD = "PRIM_CRED_CARD_TYP_CD";
    private static final String RESIDENCY_CD = "RESIDENCY_CD";
    private static final String SEC_CRED_CARD_ISS_CO_TYP_CD = "SEC_CRED_CARD_ISS_CO_TYP_CD";

    private static final String LEGAL_CARE_CD_VALUE = "false";
    private static final String EMPLOYMENT_STATUS_CD_VALUE = "TestStatusCode";
    private static final String PRIM_CRED_CARD_TYP_CD_VALUE = "TestCardTypeCode";
    private static final String RESIDENCY_CD_VALUE = "tx";
    private static final String SEC_CRED_CARD_ISS_CO_TYP_CD_VALUE = "TestCoTypeCode";

    @Test
    public void testToDto() {
        PartyEntity partyEntity = new PartyEntity();
        IndividualEntity individualEntity = new IndividualEntity();
        individualEntity.setEmploymentStatusCd(EMPLOYMENT_STATUS_CD_VALUE);
        individualEntity.setSecCredCardIssCoTypCd(SEC_CRED_CARD_ISS_CO_TYP_CD_VALUE);
        individualEntity.setResidencyCd(RESIDENCY_CD_VALUE);
        individualEntity.setPrimCredCardTypCd(PRIM_CRED_CARD_TYP_CD_VALUE);
        individualEntity.setLegalCareCd(LEGAL_CARE_CD_VALUE);

        List<PartyIdentificationExEntity> identificationEntities = new LinkedList<>();
        Individual individual = TelusIndividualIdentificationModelMapper.toDto(partyEntity, individualEntity, identificationEntities);

        assertThat(individual).isNotNull();
        List<TelusCharacteristic> characteristics = individual.getCharacteristic();
        assertThat(characteristics).hasSize(5);
        assertThat(characteristics.get(0).getName()).isEqualTo(EMPLOYMENT_STATUS_CD);
        assertThat(characteristics.get(0).getValue()).isEqualTo(EMPLOYMENT_STATUS_CD_VALUE);
        assertThat(characteristics.get(1).getName()).isEqualTo(LEGAL_CARE_CD);
        assertThat(characteristics.get(1).getValue()).isEqualTo(LEGAL_CARE_CD_VALUE);
        assertThat(characteristics.get(2).getName()).isEqualTo(PRIM_CRED_CARD_TYP_CD);
        assertThat(characteristics.get(2).getValue()).isEqualTo(PRIM_CRED_CARD_TYP_CD_VALUE);
        assertThat(characteristics.get(3).getName()).isEqualTo(RESIDENCY_CD);
        assertThat(characteristics.get(3).getValue()).isEqualTo(RESIDENCY_CD_VALUE);
        assertThat(characteristics.get(4).getName()).isEqualTo(SEC_CRED_CARD_ISS_CO_TYP_CD);
        assertThat(characteristics.get(4).getValue()).isEqualTo(SEC_CRED_CARD_ISS_CO_TYP_CD_VALUE);

    }

    @Test
    public void testToDto_withoutCharacteristics() {
        PartyEntity partyEntity = new PartyEntity();
        IndividualEntity individualEntity = new IndividualEntity();


        List<PartyIdentificationExEntity> identificationEntities = new LinkedList<>();
        Individual individual = TelusIndividualIdentificationModelMapper.toDto(partyEntity, individualEntity, identificationEntities);

        assertThat(individual).isNotNull();
        List<TelusCharacteristic> characteristics = individual.getCharacteristic();
        assertThat(characteristics).isNullOrEmpty();
    }

}
