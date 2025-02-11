package com.telus.credit.model.mapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.springframework.util.StringUtils;

import com.telus.credit.common.CreditProfileConstant;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.dao.entity.IndividualEntity;
import com.telus.credit.dao.entity.PartyEntity;
import com.telus.credit.dao.entity.PartyIdentificationEntity;
import com.telus.credit.dao.entity.PartyIdentificationExEntity;
import com.telus.credit.model.Individual;
import com.telus.credit.model.TelusCharacteristic;
import com.telus.credit.model.TelusIndividualIdentification;
import com.telus.credit.model.TimePeriod;
import com.telus.credit.model.common.IdentificationType;
import com.telus.credit.model.helper.PatchField;

public class TelusIndividualIdentificationModelMapper {

    private TelusIndividualIdentificationModelMapper() {
        // static mapper
    }

    /**
     * Map TelusIndividualIdentification to DAO entities for insert/update. In case any of TelusIndividualIdentification attributes was
     * not set in the request body, the attribute will not be included in the insert (on update they will be empty)
     *
     * @param identification
     * @return DAO entity
     */
    public static PartyIdentificationExEntity toEntity(TelusIndividualIdentification identification, List<String> encryptedAttributeDefs) {
        IdentificationType type = Objects.requireNonNull(IdentificationType
                .getIdentificationType(identification.getIdentificationType()));
        PartyIdentificationEntity identificationExEntity = new PartyIdentificationExEntity()
                .idType(type.name());

        identification.setIdentificationType(type.getDesc());

        // If validFor is included in the request body, either it is null or not null
        // identification.getValidForPatch() will return a wrapper object containing actual value.
        // This applies similarly for other attributes.
        // In create flow, null or unset are the same. In update flow, need to distinguish between null and unset
        PatchField<TimePeriod> validForPatch = identification.getValidForPatch();
        if (validForPatch != null && !validForPatch.isValueNull()) {
            TimePeriod validFor = validForPatch.get();
            identificationExEntity.validStartTs(DateTimeUtils.toUtcTimestamp(validFor.getStartDateTime()), validFor.getStartDateTimePatch() != null)
                    .validEndTs(DateTimeUtils.toUtcTimestamp(validFor.getEndDateTime()), validFor.getEndDateTimePatch() != null);
        }

        PartyIdentificationExEntity entity = (PartyIdentificationExEntity) identificationExEntity;

        IdentificationModelMapper.toEntity(entity, identification, encryptedAttributeDefs);

        return entity;
    }

    /**
     * Map Identification Entities to DTO
     *
     * @param partyEntity
     * @param identificationEntities
     * @return mapped DTO
     */
    public static Individual toDto(PartyEntity partyEntity, IndividualEntity individualEntity, List<PartyIdentificationExEntity> identificationEntities) {
        Individual individual = new Individual();
        individual.setId(partyEntity.getPartyId());
        individual.setRole(partyEntity.getPartyRole());

        individual.setIndividualIdentification(new ArrayList<>());
        if (identificationEntities != null) {
	        identificationEntities.forEach(ide -> {
	            individual.getIndividualIdentification().add(IdentificationModelMapper.toDtoIndividual(ide));
	        });
        }
        individual.setContactMedium(new ArrayList<>());

        if (individualEntity != null) {
            individual.setBirthDate(DateTimeUtils.toUtcDateString(individualEntity.getBirthDate()));
       
	        List<TelusCharacteristic> characteristicList = new LinkedList<>();
	        if(!StringUtils.isEmpty(individualEntity.getEmploymentStatusCd())) {
	            TelusCharacteristic characteristic = new TelusCharacteristic();
	            characteristic.setName(CreditProfileConstant.EMPLOYMENT_STATUS_CD);
	            characteristic.setValue(individualEntity.getEmploymentStatusCd());
	            characteristic.setValueType("String");
	            characteristic.setType("TelusCharacteristic");
	            characteristicList.add(characteristic);
	        }
	        if(!StringUtils.isEmpty(individualEntity.getLegalCareCd())) {
	            TelusCharacteristic characteristic = new TelusCharacteristic();
	            characteristic.setName(CreditProfileConstant.LEGAL_CARE_CD);
	            characteristic.setValue(individualEntity.getLegalCareCd());
	            characteristic.setValueType("String");
	            characteristic.setType("TelusCharacteristic");
	            characteristicList.add(characteristic);
	        }
	        if(!StringUtils.isEmpty(individualEntity.getPrimCredCardTypCd())) {
	            TelusCharacteristic characteristic = new TelusCharacteristic();
	            characteristic.setName(CreditProfileConstant.PRIM_CRED_CARD_TYP_CD);
	            characteristic.setValue(individualEntity.getPrimCredCardTypCd());
	            characteristic.setValueType("String");
	            characteristic.setType("TelusCharacteristic");
	            characteristicList.add(characteristic);
	        }
	        if(!StringUtils.isEmpty(individualEntity.getResidencyCd())) {
	            TelusCharacteristic characteristic = new TelusCharacteristic();
	            characteristic.setName(CreditProfileConstant.RESIDENCY_CD);
	            characteristic.setValue(individualEntity.getResidencyCd());
	            characteristic.setValueType("String");
	            characteristic.setType("TelusCharacteristic");
	            characteristicList.add(characteristic);
	        }
	        if(!StringUtils.isEmpty(individualEntity.getSecCredCardIssCoTypCd())) {
	            TelusCharacteristic characteristic = new TelusCharacteristic();
	            characteristic.setName(CreditProfileConstant.SEC_CRED_CARD_ISS_CO_TYP_CD);
	            characteristic.setValue(individualEntity.getSecCredCardIssCoTypCd());
	            characteristic.setValueType("String");
	            characteristic.setType("TelusCharacteristic");
	            characteristicList.add(characteristic);
	        }
	        
	        individual.setCharacteristic(characteristicList);
        } 
        return individual;
    }
}
