package com.telus.credit.model.mapper;

import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.dao.entity.PartyContactMediumEntity;
import com.telus.credit.model.ContactMedium;
import com.telus.credit.model.MediumCharacteristic;
import com.telus.credit.model.TimePeriod;
import com.telus.credit.model.helper.PatchField;

public class ContactMediumModelMapper {
    private ContactMediumModelMapper() {
        // static mapper
    }

    /**
     * Map ContactMedium to DAO entity for insert/update. In case any of ContactMedium attributes was
     * not set in the request body, the attribute will not be included in the update
     *
     * @param contactMedium
     * @return DAO entity
     */
    public static PartyContactMediumEntity toEntity(ContactMedium contactMedium) {
        // If characteristic is included in the request body, either it is null or not null
        // contactMedium.getCharacteristicPatch will return a wrapper object containing actual value.
        // This applies similarly for other attributes
        // In create flow, null or unset are the same. In update flow, need to distinguish between null and unset
        PatchField<MediumCharacteristic> characteristicOptional = contactMedium.getCharacteristicPatch();

        PartyContactMediumEntity entity = new PartyContactMediumEntity()
                .mediumType(contactMedium.getMediumType(), contactMedium.getMediumTypePatch() != null)
                .preffered(contactMedium.getPreferred(), contactMedium.getPreferredPatch() != null);

        if (characteristicOptional != null && !characteristicOptional.isValueNull()) {
            MediumCharacteristic characteristic = characteristicOptional.get();
            entity.city(characteristic.getCity(), characteristic.getCityPatch() != null)
                    .contactType(characteristic.getContactType(), characteristic.getContactTypePatch() != null)
                    .countryCode(characteristic.getCountry(), characteristic.getCountryPatch() != null)
                    .postCode(characteristic.getPostCode(), characteristic.getPostCodePatch() != null)
                    .stateProvinceCode(characteristic.getStateOrProvince(), characteristic.getStateOrProvincePatch() != null)
                    .street1(characteristic.getStreet1(), characteristic.getStreet1Patch() != null)
                    .street2(characteristic.getStreet2(), characteristic.getStreet2Patch() != null)
                    .street3(characteristic.getStreet3(), characteristic.getStreet3Patch() != null)
                    .street4(characteristic.getStreet4(),characteristic.getStreet4Patch() != null)
                    .street5(characteristic.getStreet5(), characteristic.getStreet5Patch() != null)
                    .email(characteristic.getEmail(), characteristic.getEmailPatch() != null)
                    .phoneNumber(characteristic.getPhoneNumber(), characteristic.getPhoneNumberPatch() != null);
        }

        PatchField<TimePeriod> validForOptional = contactMedium.getValidForPatch();
        if (validForOptional != null && !validForOptional.isValueNull()) {
            TimePeriod validFor = contactMedium.getValidForPatch().get();
            entity.validStartTs(DateTimeUtils.toUtcTimestamp(validFor.getStartDateTime()), validFor.getStartDateTimePatch() != null)
                .validEndTs(DateTimeUtils.toUtcTimestamp(validFor.getEndDateTime()), validFor.getEndDateTimePatch() != null);
        }

        return entity;
    }

    /**
     * Map DAO entity to DTO
     *
     * @param entity
     * @return dto
     */
    public static ContactMedium toDto(PartyContactMediumEntity entity) {
        ContactMedium medium = new ContactMedium();
        medium.setId(entity.getContactMediumId());
        medium.setMediumType(entity.getMediumType());
        medium.setPreferred(entity.getPreffered());

        MediumCharacteristic characteristic = new MediumCharacteristic();
        medium.setCharacteristic(characteristic);

        characteristic.setCity(entity.getCity());
        characteristic.setContactType(entity.getContactType());
        characteristic.setCountry(entity.getCountryCode());
        characteristic.setPostCode(entity.getPostCode());
        characteristic.setStateOrProvince(entity.getStateProvinceCode());
        characteristic.setStreet1(entity.getStreet1());
        characteristic.setStreet2(entity.getStreet2());
        characteristic.setStreet3(entity.getStreet3());
        characteristic.setStreet4(entity.getStreet4());
        characteristic.setStreet5(entity.getStreet5());
        characteristic.setEmail(entity.getEmail());
        characteristic.setPhoneNumber(entity.getPhoneNumber());

        if (entity.getValidEndTs() != null || entity.getValidStartTs() != null) {
            TimePeriod timePeriod = new TimePeriod();
            timePeriod.setStartDateTime(DateTimeUtils.toUtcString(entity.getValidStartTs()));
            timePeriod.setEndDateTime(DateTimeUtils.toUtcString(entity.getValidEndTs()));
            medium.setValidFor(timePeriod);
        }

        return medium;
    }
}
