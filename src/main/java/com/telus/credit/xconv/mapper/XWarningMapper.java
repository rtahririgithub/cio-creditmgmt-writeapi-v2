package com.telus.credit.xconv.mapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.telus.credit.common.CommonHelper;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.dao.entity.CreditWarningHistoryEntity;
import com.telus.credit.dao.entity.XWarningEntity;
import com.telus.credit.exceptions.CryptoException;
import com.telus.credit.model.TelusCreditDecisionWarning;
import com.telus.credit.model.TimePeriod;

@Component
public class XWarningMapper {

    private static CryptoService cryptoService;

    @Autowired
    private void setCryptoService(CryptoService cryptoService) {
        XWarningMapper.cryptoService = cryptoService;
    }

    public static CreditWarningHistoryEntity toCreditWarningEntity(XWarningEntity warning) {
        CreditWarningHistoryEntity entity = new CreditWarningHistoryEntity();

        try {
            entity.warningLegacyId(warning.getWarningLegacyId())
                    .warningCategoryCd(cryptoService.encryptOrNull(warning.getWarningCategoryCd()))
                    .warningCd(cryptoService.encryptOrNull(warning.getWarningCd()))
                    .warningTypeCd(cryptoService.encryptOrNull(warning.getWarningTypeCd()))
                    .warningItemTypeCd(cryptoService.encryptOrNull(warning.getWarningItemTypeCd()))
                    .warningDetectionTs(warning.getWarningDetectionTs())
                    .warningStatusCd(cryptoService.encryptOrNull(warning.getStatusId()))
                    .creditAssessmentId((Long) CommonHelper.safeTransform(warning.getCreditAssessmentId(), v -> Long.parseLong(StringUtils.trim(v)), null, null))
                    .approvalDt(warning.getApprovalDt())
                    .approvalExternalId((Long) CommonHelper.safeTransform(warning.getApprovalExternalId(), v -> Long.parseLong(StringUtils.trim(v)), null, null))
                    .memoTypeCd(warning.getMemoTypeCd());

            String resolvedFlag = StringUtils.trimToNull(warning.getResolvedFlag());
            if (resolvedFlag != null) {
                entity.resolvedFlag("Y".equals(resolvedFlag));
            }
        } catch (Exception e) {
            throw new CryptoException(e);
        }

        entity.setValidEndTs(warning.getValidEndTs());
        entity.setValidStartTs(warning.getValidStartTs());

        return entity;
    }
    public static TelusCreditDecisionWarning toCreditWarning(XWarningEntity xWarning) {
    	TelusCreditDecisionWarning cpWarning = new TelusCreditDecisionWarning();

        try {
            cpWarning.setWarningHistoryLegacyId(xWarning.getWarningLegacyId());
            cpWarning.setWarningCategoryCd(xWarning.getWarningCategoryCd());
            cpWarning.setWarningCd(xWarning.getWarningCd());
            cpWarning.setWarningTypeCd(xWarning.getWarningTypeCd());
            cpWarning.setWarningItemTypeCd(xWarning.getWarningItemTypeCd());
            cpWarning.setWarningDetectionTs(DateTimeUtils.toUtcString(xWarning.getWarningDetectionTs()));
            cpWarning.setWarningStatusCd(xWarning.getStatusId());
            cpWarning.setApprovalCreditAssessmentId((Long) CommonHelper.safeTransform(xWarning.getCreditAssessmentId(), v -> Long.parseLong(StringUtils.trim(v)), null, null));
            
            cpWarning.setApprovalTs(DateTimeUtils.toUtcString(xWarning.getApprovalDt()));
            cpWarning.setApprovalExternalId((Long) CommonHelper.safeTransform(xWarning.getApprovalExternalId(), v -> Long.parseLong(StringUtils.trim(v)), null, null));

            String resolvedFlag = StringUtils.trimToNull(xWarning.getResolvedFlag());
            if (resolvedFlag != null) {
                cpWarning.setResolvedPermanentlyInd("Y");
            }
        } catch (Exception e) {
            throw new CryptoException(e);
        }

		TimePeriod timePeriod = new TimePeriod();
		timePeriod.setStartDateTime(DateTimeUtils.toUtcString(xWarning.getValidStartTs()));
		timePeriod.setEndDateTime(DateTimeUtils.toUtcString(xWarning.getValidEndTs()));
		cpWarning.setValidFor(timePeriod);

        return cpWarning;
    }    
}
