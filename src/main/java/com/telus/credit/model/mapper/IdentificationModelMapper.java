package com.telus.credit.model.mapper;

import static com.telus.credit.model.mapper.IdentificationCharacteristicMapper.mapAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.dao.entity.IdentificationCharEntity;
import com.telus.credit.dao.entity.IdentificationCharHashEntity;
import com.telus.credit.dao.entity.PartyIdentificationExEntity;
import com.telus.credit.model.OrganizationIdentification;
import com.telus.credit.model.TelusIndividualIdentification;
import com.telus.credit.model.TelusIndividualIdentificationCharacteristic;
import com.telus.credit.model.TimePeriod;
import com.telus.credit.model.common.IdentificationType;

public class IdentificationModelMapper {

    public static final String IDENTIFICATION_ID = "identificationId";
    public static final String ISSUING_AUTHORITY = "issuingAuthority";
    public static final String ISSUING_DATE = "issuingDate";
    public static final String PROVINCE_CD = "provinceCd";
    public static final String COUNTRY_CD = "countryCd";

    private IdentificationModelMapper() {
        // static mapper
    }

    /**
     * As identification attributes are dynamic, each attribute will be mapped into one entity.
     * This method create PartyIdentificationEntity for Individual and a list of IdentificationCharEntity/IdentificationCharHashEntity
     * based on identification types
     */
    public static void toEntity(PartyIdentificationExEntity resultHolder, TelusIndividualIdentification identification, List<String> encryptedAttrs) {
        TelusIndividualIdentificationCharacteristic telusCharacteristic = identification.getTelusCharacteristic();

        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        switch (Objects.requireNonNull(IdentificationType.getIdentificationType(identification.getIdentificationType()))) {
            case DL:
            case HC:
            case PRV:
                if (telusCharacteristic != null && telusCharacteristic.getProvinceCdPatch() != null) {
                    mapAttribute(PROVINCE_CD, telusCharacteristic.getProvinceCd(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
                }
                if (identification.getIssuingAuthorityPatch() != null) {
                    mapAttribute(ISSUING_AUTHORITY, identification.getIssuingAuthority(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
                }
                break;
            case PSP:
                if (telusCharacteristic != null && telusCharacteristic.getCountryCdPatch() != null) {
                    mapAttribute(COUNTRY_CD, telusCharacteristic.getCountryCd(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
                }
                if (identification.getIssuingAuthorityPatch() != null) {
                    mapAttribute(ISSUING_AUTHORITY, identification.getIssuingAuthority(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
                }
                break;
		default:
			break;
        }

        if (identification.getIdentificationIdPatch() != null) {
            mapAttribute(IDENTIFICATION_ID, identification.getIdentificationId(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        }
        resultHolder.setCharacteristic(characteristicEntities);
        resultHolder.setHashedCharacteristics(characteristicHashEntities);
    }

    /**
     * As identification attributes are dynamic, each attribute will be mapped into one entity.
     * This method create PartyIdentificationEntity for Organization and a list of IdentificationCharEntity/IdentificationCharHashEntity
     * based on identification types
     */
    public static void toEntity(PartyIdentificationExEntity resultHolder, OrganizationIdentification identification, List<String> encryptedAttrs) {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        mapAttribute(IDENTIFICATION_ID, identification.getIdentificationId(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        mapAttribute(ISSUING_AUTHORITY, identification.getIssuingAuthority(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        mapAttribute(ISSUING_DATE, identification.getIssuingDate(), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        resultHolder.setCharacteristic(characteristicEntities);
        resultHolder.setHashedCharacteristics(characteristicHashEntities);
    }

    /**
     * Map PartyIdentificationExEntity Entities and the list of characteristic to DTO
     *
     * @param entity
     * @return mapped DTO
     */
    public static TelusIndividualIdentification toDtoIndividual(PartyIdentificationExEntity entity) {
        TelusIndividualIdentification identification = new TelusIndividualIdentification();
        TelusIndividualIdentificationCharacteristic characteristic = new TelusIndividualIdentificationCharacteristic();

        //identification.setIdentificationType(Objects.requireNonNull(IdentificationType.getIdentificationType(entity.getIdType())).getDesc());
        IdentificationType identificationType = IdentificationType.getIdentificationType(entity.getIdType()) ;        
        identification.setIdentificationType(Objects.requireNonNull(identificationType.name()));

        characteristic.setIdentificationTypeCd(entity.getIdType());

        identification.setTelusCharacteristic(characteristic);

        if (entity.getValidEndTs() != null || entity.getValidStartTs() != null) {
            TimePeriod timePeriod = new TimePeriod();
            timePeriod.setStartDateTime(DateTimeUtils.toUtcString(entity.getValidStartTs()));
            timePeriod.setEndDateTime(DateTimeUtils.toUtcString(entity.getValidEndTs()));
            identification.setValidFor(timePeriod);
        }

        entity.getCharacteristic().forEach(ch -> {
            switch (ch.getKey()) {
                case IDENTIFICATION_ID:
                    identification.setIdentificationId(ch.getValue());
                    break;
                case ISSUING_AUTHORITY:
                    identification.setIssuingAuthority(ch.getValue());
                    break;
                case ISSUING_DATE:
                    identification.setIssuingDate(ch.getValue());
                    break;
                case COUNTRY_CD:
                    characteristic.setCountryCd(ch.getValue());
                    break;
                case PROVINCE_CD:
                    characteristic.setProvinceCd(ch.getValue());
                    break;
            }
        });

        entity.getHashedCharacteristics().forEach(ch -> {
            if (IDENTIFICATION_ID.equals(ch.getKey())) {
                identification.setIdentificationIdHashed(ch.getValue());
            }
        });

        return identification;
    }

    /**
     * Map PartyIdentificationExEntity Entities and the list of characteristic to DTO
     *
     * @param entity
     * @return mapped DTO
     */
    public static OrganizationIdentification toDtoOrganization(PartyIdentificationExEntity entity) {
        OrganizationIdentification identification = new OrganizationIdentification();

        IdentificationType identificationType = IdentificationType.getIdentificationType(entity.getIdType()) ;        
        identification.setIdentificationType(Objects.requireNonNull(identificationType.name()));

        if (entity.getValidEndTs() != null || entity.getValidStartTs() != null) {
            TimePeriod timePeriod = new TimePeriod();
            timePeriod.setStartDateTime(DateTimeUtils.toUtcString(entity.getValidStartTs()));
            timePeriod.setEndDateTime(DateTimeUtils.toUtcString(entity.getValidEndTs()));
            identification.setValidFor(timePeriod);
        }

        entity.getCharacteristic().forEach(ch -> {
            switch (ch.getKey()) {
                case IDENTIFICATION_ID:
                    identification.setIdentificationId(ch.getValue());
                    break;
                case ISSUING_AUTHORITY:
                    identification.setIssuingAuthority(ch.getValue());
                    break;
                case ISSUING_DATE:
                    identification.setIssuingDate(ch.getValue());
            }
        });

        
        entity.getHashedCharacteristics().forEach(ch -> {
            if (IDENTIFICATION_ID.equals(ch.getKey())) {
                identification.setIdentificationIdHashed(ch.getValue());
            }
        });        

        return identification;
    }
}
