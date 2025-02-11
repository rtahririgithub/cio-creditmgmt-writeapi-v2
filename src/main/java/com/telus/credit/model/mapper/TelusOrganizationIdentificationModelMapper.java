package com.telus.credit.model.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.dao.entity.OrganizationEntity;
import com.telus.credit.dao.entity.PartyEntity;
import com.telus.credit.dao.entity.PartyIdentificationEntity;
import com.telus.credit.dao.entity.PartyIdentificationExEntity;
import com.telus.credit.model.Organization;
import com.telus.credit.model.OrganizationIdentification;
import com.telus.credit.model.TimePeriod;
import com.telus.credit.model.common.IdentificationType;
import com.telus.credit.model.helper.PatchField;

public class TelusOrganizationIdentificationModelMapper {

    private TelusOrganizationIdentificationModelMapper() {
        // static mapper
    }

    /**
     * Map OrganizationIdentification to DAO entities for insert/update. In case any of OrganizationIdentification attributes was
     * not set in the request body, the attribute will not be included in the insert (on update they will be empty)
     *
     * @param identification
     * @return DAO entity
     */
    public static PartyIdentificationExEntity toEntity(OrganizationIdentification identification, List<String> encryptedAttributeDefs) {
        IdentificationType type = Objects.requireNonNull(IdentificationType.getIdentificationType(identification.getIdentificationType()));
        PartyIdentificationEntity identificationExEntity = new PartyIdentificationExEntity().idType(type.name());

        identification.setIdentificationType(type.toString());

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
    public static Organization toDto(PartyEntity partyEntity,  OrganizationEntity organizationEntity, List<PartyIdentificationExEntity> identificationEntities) {
        Organization organization = new Organization();
        organization.setId(partyEntity.getPartyId());
        organization.setRole(partyEntity.getPartyRole());

        if (organizationEntity != null) {
        	organization.setBirthDate(DateTimeUtils.toUtcDateString(organizationEntity.getBirthDate()));
        }
        
        organization.setOrganizationIdentification(new ArrayList<>());
        identificationEntities.forEach(ide -> organization.getOrganizationIdentification().add(IdentificationModelMapper.toDtoOrganization(ide)));

        organization.setContactMedium(new ArrayList<>());
        return organization;
    }
}
