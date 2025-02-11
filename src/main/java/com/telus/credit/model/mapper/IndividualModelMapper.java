package com.telus.credit.model.mapper;

import java.util.List;

import org.springframework.util.CollectionUtils;

import com.telus.credit.common.CreditProfileConstant;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.dao.entity.IndividualEntity;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCharacteristic;
import com.telus.credit.model.helper.PatchField;

public class IndividualModelMapper {

    public static IndividualEntity toEntity(String partyId, RelatedPartyToPatch engagedParty, TelusChannel auditCharacteristic) {
        IndividualEntity individualEntity = new IndividualEntity()
                .partyId(partyId)
                .birthDate(DateTimeUtils.toUtcDate(engagedParty.getBirthDate()))
                .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                .originatorAppId(auditCharacteristic.getOriginatorAppId())
                .channelOrgId(auditCharacteristic.getChannelOrgId());
        PatchField<List<TelusCharacteristic>> characteristicPatch = engagedParty.getCharacteristicsPatch();
        if (characteristicPatch != null && !characteristicPatch.isValueNull()) {
            List<TelusCharacteristic> characteristicList = characteristicPatch.get();
            if (!CollectionUtils.isEmpty(characteristicList)) {
                characteristicList.forEach(characteristic -> {
                    switch (characteristic.getName()) {
                        case CreditProfileConstant.EMPLOYMENT_STATUS_CD:
                            individualEntity.setEmploymentStatusCd(characteristic.getValue());
                            break;
                        case CreditProfileConstant.LEGAL_CARE_CD:
                            individualEntity.setLegalCareCd(characteristic.getValue());
                            break;
                        case CreditProfileConstant.PRIM_CRED_CARD_TYP_CD:
                            individualEntity.setPrimCredCardTypCd(characteristic.getValue());
                            break;
                        case CreditProfileConstant.RESIDENCY_CD:
                            individualEntity.setResidencyCd(characteristic.getValue());
                            break;
                        case CreditProfileConstant.SEC_CRED_CARD_ISS_CO_TYP_CD:
                            individualEntity.setSecCredCardIssCoTypCd(characteristic.getValue());
                            break;
                    }
                });
            }
        }
        return individualEntity;
    }
}
