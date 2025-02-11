package com.telus.credit.model.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.dao.entity.CreditWarningHistoryEntity;
import com.telus.credit.exceptions.CryptoException;
import com.telus.credit.model.TelusCreditDecisionWarning;
import com.telus.credit.model.TimePeriod;
import com.telus.credit.model.helper.PatchField;

@Component
public class CreditWarningHistoryModelMapper {

    private static CryptoService cryptoService;

    @Autowired
    public void setEncryptionService(CryptoService cryptoService) {
        CreditWarningHistoryModelMapper.cryptoService = cryptoService;
    }

    /**
     * Map TelusCreditDecisionWarning to DAO entity for insert/update. In case any of TelusCreditDecisionWarning attributes was
     * not set in the request body, the attribute will not be included in the update
     *
     * @param warning
     * @return DAO entity
     */
    public static CreditWarningHistoryEntity toEntity(TelusCreditDecisionWarning warning) {
        CreditWarningHistoryEntity entity = new CreditWarningHistoryEntity();

        try {
            // If WarningHistoryLegacyId is included in the request body, either it is null or not null
            // warning.getWarningHistoryLegacyIdPatch will return a wrapper object containing actual value.
            // This applies similarly for other attributes
            // In create flow, null or unset are the same. In update flow, need to distinguish between null and unset
            entity
            	.warningLegacyId(warning.getWarningHistoryLegacyId(), warning.getWarningHistoryLegacyIdPatch() != null)
            	
                .warningCategoryCd(cryptoService.encryptOrNull(warning.getWarningCategoryCd()), warning.getWarningCategoryCdPatch() != null)
                .warningCd(cryptoService.encryptOrNull(warning.getWarningCd()), warning.getWarningCdPatch() != null)
                .warningTypeCd(cryptoService.encryptOrNull(warning.getWarningTypeCd()), warning.getWarningTypeCdPatch() != null)
                .warningItemTypeCd(cryptoService.encryptOrNull(warning.getWarningItemTypeCd()), warning.getWarningItemTypeCdPatch() != null)
                .warningDetectionTs(DateTimeUtils.toUtcTimestamp(warning.getWarningDetectionTs()), warning.getWarningDetectionDatePatch() != null)
                .warningStatusCd(cryptoService.encryptOrNull(warning.getWarningStatusCd()), warning.getWarningStatusCdPatch() != null)
                .resolvedFlag(warning.getResolvedPermanentlyInd() != null ? Boolean.parseBoolean(warning.getResolvedPermanentlyInd()) : null, warning.getResolvedPermanentlyIndPatch() != null)
                .creditAssessmentId(warning.getApprovalCreditAssessmentId(), warning.getCreditAssessmentIdPatch() != null)
                .approvalDt(DateTimeUtils.toUtcTimestamp(warning.getApprovalTs()), warning.getApprovalDatePatch() != null)
                .approvalExternalId(warning.getApprovalExternalId(), warning.getApprovalExternalIdPatch() != null)
                //.memoTypeCd(warning.getMemoTypeCd(), warning.getMemoTypeCdPatch() != null)
                ;
        } catch (Exception e) {
            throw new CryptoException(e);
        }

        PatchField<TimePeriod> validForOptional = warning.getValidForPatch();
        if (validForOptional != null && !validForOptional.isValueNull()) {
            TimePeriod validFor = warning.getValidForPatch().get();
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
    public static TelusCreditDecisionWarning toDto(CreditWarningHistoryEntity entity) {
        TelusCreditDecisionWarning warning = new TelusCreditDecisionWarning();
        warning.setId(entity.getWarningId());
       warning.setWarningHistoryLegacyId(entity.getWarningLegacyId());
        warning.setWarningCategoryCd(entity.getWarningCategoryCd());
        warning.setWarningCd(entity.getWarningCd());
        warning.setWarningTypeCd(entity.getWarningTypeCd());
        warning.setWarningItemTypeCd(entity.getWarningItemTypeCd());
        warning.setWarningDetectionTs(DateTimeUtils.toUtcString(entity.getWarningDetectionTs()));
        warning.setWarningStatusCd(entity.getWarningStatusCd());
        warning.setResolvedPermanentlyInd(entity.getResolvedFlag() != null ? Boolean.toString(entity.getResolvedFlag()) : null);
        warning.setApprovalCreditAssessmentId(entity.getCreditAssessmentId());
        warning.setApprovalTs(DateTimeUtils.toUtcString(entity.getApprovalDt()));
        warning.setApprovalExternalId(entity.getApprovalExternalId());
       // warning.setMemoTypeCd(entity.getMemoTypeCd());

        if (entity.getValidEndTs() != null || entity.getValidStartTs() != null) {
            TimePeriod timePeriod = new TimePeriod();
            timePeriod.setStartDateTime(DateTimeUtils.toUtcString(entity.getValidStartTs()));
            timePeriod.setEndDateTime(DateTimeUtils.toUtcString(entity.getValidEndTs()));
            warning.setValidFor(timePeriod);
        }

        return warning;
    }
}
