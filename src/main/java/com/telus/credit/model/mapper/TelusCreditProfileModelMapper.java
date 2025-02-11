package com.telus.credit.model.mapper;

import com.telus.credit.common.CreditProfileConstant;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.dao.entity.CreditProfileEntity;
import com.telus.credit.exceptions.CreditException;
import com.telus.credit.exceptions.CryptoException;
import com.telus.credit.model.RiskLevelRiskAssessment;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCharacteristic;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.model.TelusCreditProfileCharacteristic;
import com.telus.credit.model.TimePeriod;
import com.telus.credit.model.helper.PatchField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_8001;
import static com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_8001_MSG;
import static com.telus.credit.util.Utils.getCharacteristicValue;

@Component
public class TelusCreditProfileModelMapper {

    private static CryptoService cryptoService;

    @Autowired
    public void setEncryptionService(CryptoService cryptoService) {
        TelusCreditProfileModelMapper.cryptoService = cryptoService;
    }

    /**
     * Map TelusCreditProfile to DAO entity for insert/update. In case any of TelusCreditProfile attributes was
     * not set in the request body, the attribute will not be included in the update.
     *
     * @param creditProfile
     * @return DAO entity
     */
    public static CreditProfileEntity toEntity_v1(TelusCreditProfile creditProfile) {
        // If telusCharacteristic is included in the request body, either it is null or not null
        // creditProfile.getTelusCharacteristicPatch() will return a wrapper object containing actual value.
        // This applies similarly for other attributes.
        // In create flow, null or unset are the same. In update flow, need to distinguish between null and unset
        PatchField<TelusCreditProfileCharacteristic> telusCharacteristicPatch = creditProfile.getTelusCharacteristicPatch();

        CreditProfileEntity entity = new CreditProfileEntity()
                .creditProfileTs(DateTimeUtils.toUtcTimestamp(creditProfile.getCreatedTs()), creditProfile.getCreditProfileDatePatch() != null);

        
        try {
            if (creditProfile.getCreditRiskLevelNum() != null) {
                entity.creditRiskRating(
                		cryptoService.encryptOrNull(creditProfile.getCreditRiskLevelNum()),
                        creditProfile.getCreditRiskRatingPatch() != null
                        );
            }
            if (creditProfile.getPrimaryCreditScoreCd() != null) {
                entity.creditScore(cryptoService.encryptOrNull(creditProfile.getPrimaryCreditScoreCd()),
                        creditProfile.getCreditScorePatch() != null);
            }
        } catch (Exception e) {
            throw new CryptoException(e);
        }

        if (telusCharacteristicPatch != null && !telusCharacteristicPatch.isValueNull()) {
            TelusCreditProfileCharacteristic telusCharacteristic = telusCharacteristicPatch.get();
            try {
                entity
                .creditProfileIdLegacy(telusCharacteristic.getCreditProfileLegacyId(), telusCharacteristic.getCreditProfileLegacyIdPatch() != null)
//                    .creditScore(telusCharacteristic.getPrimaryCreditScoreCd())
                        .creditScoreTypeCode(telusCharacteristic.getPrimaryCreditScoreTypeCd(), telusCharacteristic.getPrimaryCreditScoreTypeCdPatch() != null)
                        .agencyDecisionCode(telusCharacteristic.getBureauDecisionCode(), telusCharacteristic.getBureauDecisionCodePatch() != null)
                        .creditProgramName(telusCharacteristic.getCreditProgramName(), telusCharacteristic.getCreditProgramNamePatch() != null)
                        .creditClassCode(cryptoService.encryptOrNull(telusCharacteristic.getCreditClassCd()), telusCharacteristic.getCreditClassCdPatch() != null)
                        .creditClassTs(DateTimeUtils.toUtcTimestamp(telusCharacteristic.getCreditClassDate()), telusCharacteristic.getCreditClassDatePatch() != null)
                        .creditDecisionCode(cryptoService.encryptOrNull(telusCharacteristic.getCreditDecisionCd()), telusCharacteristic.getCreditDecisionCdPatch() != null)
                        .creditDecisionTs(DateTimeUtils.toUtcTimestamp(telusCharacteristic.getCreditDecisionDate()), telusCharacteristic.getCreditDecisionDatePatch() != null)
//                    .creditRiskRating(telusCharacteristic.getRiskLevelNumber())
                        .riskLevelDecisionCode(cryptoService.encryptOrNull(telusCharacteristic.getRiskLevelDecisionCd()), telusCharacteristic.getRiskLevelDecisionCdPatch() != null)
                        .riskLevelTs(DateTimeUtils.toUtcTimestamp(telusCharacteristic.getRiskLevelDt()), telusCharacteristic.getRiskLevelDtPatch() != null)
                        .clpRatePlanAmt(telusCharacteristic.getClpRatePlanAmt(), telusCharacteristic.getClpRatePlanAmtPatch() != null)
                        .clpContractTerm(telusCharacteristic.getClpContractTerm(), telusCharacteristic.getClpContractTermPatch() != null)
                        .clpCreditLimitAmt(telusCharacteristic.getClpCreditLimit(), telusCharacteristic.getClpCreditLimitPatch() != null)
                        .securityDepAmt(telusCharacteristic.getAverageSecurityDepositAmt(), telusCharacteristic.getAverageSecurityDepositAmtPatch() != null)
                        .creditAssessmentId(telusCharacteristic.getCreditAssessmentId(), telusCharacteristic.getCreditAssessmentIdPatch() != null);

            } catch (Exception e) {
                throw new CryptoException(e);
            }
        }

        PatchField<TimePeriod> validForPatch = creditProfile.getValidForPatch();
        if (validForPatch != null && !validForPatch.isValueNull()) {
            entity
            	.validStartTs(DateTimeUtils.toUtcTimestamp(validForPatch.get().getStartDateTime()), validForPatch.get().getStartDateTimePatch() != null)
                .validEndTs(DateTimeUtils.toUtcTimestamp(validForPatch.get().getEndDateTime()), validForPatch.get().getEndDateTimePatch() != null);
        }
        entity.productCategoryBoltOn(Boolean.valueOf(creditProfile.getBoltonInd()), creditProfile.getBoltonIndPatch() != null);
        return entity;
    }
    /*
       an attribute is updated only if it has a value( a value but null)   
       If telusCharacteristic is included in the request body, either it is null or not null
        creditProfile.getTelusCharacteristicPatch() will return a wrapper object containing actual value.
        This applies similarly for other attributes.
        In create flow, null or unset are the same. In update flow, need to distinguish between null and unset
     */
    public static CreditProfileEntity toCreditProfileEntity(TelusCreditProfile creditProfile) {

        TelusChannel aTelusChannel = creditProfile.getChannel();
        if (aTelusChannel == null) {
            aTelusChannel = new TelusChannel();
        }
        CreditProfileEntity creditProfileEntity = new CreditProfileEntity();

        if (creditProfile.getCreatedTs() != null) {
            creditProfileEntity.createdTs(DateTimeUtils.toUtcTimestamp(creditProfile.getCreatedTs()));
            creditProfileEntity.updatedTs(DateTimeUtils.toUtcTimestamp(creditProfile.getCreatedTs()));
        }

        if (creditProfile.getCreationTs() != null) {
            creditProfileEntity.creditProfileTs(DateTimeUtils.toUtcTimestamp(creditProfile.getCreationTs()));
        }


        Long riskLevelRiskAssessmentId = (creditProfile.getRiskLevelRiskAssessment() != null) ? creditProfile.getRiskLevelRiskAssessment().getId() : null;
        try {
            if (creditProfile.getCreditRiskLevelNum() != null) {
                creditProfileEntity.creditRiskRating(cryptoService.encryptOrNull(creditProfile.getCreditRiskLevelNum()),creditProfile.getCreditRiskRatingPatch() != null);
            }
            if (creditProfile.getPrimaryCreditScoreCd() != null) {
                creditProfileEntity.creditScore(cryptoService.encryptOrNull(creditProfile.getPrimaryCreditScoreCd()),creditProfile.getCreditScorePatch() != null);
            }
        } catch (Exception e) {
            throw new CryptoException(e);
        }
        String startDateTime = (creditProfile.getValidFor() != null) ? creditProfile.getValidFor().getStartDateTime() : null;
        String endDateTime = (creditProfile.getValidFor() != null) ? creditProfile.getValidFor().getEndDateTime() : null;

        
        try {
            creditProfileEntity
                    .creditProfileIdLegacy(creditProfile.getCreditProfileLegacyId(), creditProfile.getCreditProfileLegacyIdPatch() != null)
                    .creditScoreTypeCode(creditProfile.getPrimaryCreditScoreTypeCd(), creditProfile.getPrimaryCreditScoreTypeCdPatch() != null)
                    .creditClassTs(DateTimeUtils.toUtcTimestamp(creditProfile.getCreditClassDate()), creditProfile.getCreditClassDatePatch() != null)
                    .agencyDecisionCode(creditProfile.getBureauDecisionCode(), creditProfile.getBureauDecisionCodePatch() != null)
                    .creditProgramName(creditProfile.getCreditProgramName(), creditProfile.getCreditProgramNamePatch() != null)
                    .creditDecisionTs(DateTimeUtils.toUtcTimestamp(creditProfile.getCreditDecisionDate()), creditProfile.getCreditDecisionDatePatch() != null)
                    .riskLevelTs(DateTimeUtils.toUtcTimestamp(creditProfile.getRiskLevelDt()), creditProfile.getRiskLevelDtPatch() != null)
                    .clpRatePlanAmt(creditProfile.getClpRatePlanAmt(), creditProfile.getClpRatePlanAmtPatch() != null)
                    .clpContractTerm(creditProfile.getClpContractTerm(), creditProfile.getClpContractTermPatch() != null)
                    .clpCreditLimitAmt(creditProfile.getClpCreditLimitAmt(), creditProfile.getClpCreditLimitAmtPatch() != null)
                    .securityDepAmt(creditProfile.getAverageSecurityDepositAmt(), creditProfile.getAverageSecurityDepositAmtPatch() != null)                    
                    .creditAssessmentId(riskLevelRiskAssessmentId, creditProfile.getRiskLevelRiskAssessmentPatch() != null)
                    .applicationSubProvCd(creditProfile.getApplicationProvinceCd(), creditProfile.getApplicationProvinceCdPatch() != null)
                    .creditCheckConsentCd(creditProfile.getCreditProfileConsentCd(), creditProfile.getCreditProfileConsentCdPatch() != null)
                    .lineOfBusiness(creditProfile.getLineOfBusiness(), creditProfile.getLineOfBusinessPatch() != null)


                    .validStartTs(DateTimeUtils.toUtcTimestamp(startDateTime), (creditProfile.getValidFor()!= null && creditProfile.getValidFor().getStartDateTimePatch() != null) )
                    .validEndTs(DateTimeUtils.toUtcTimestamp(endDateTime), (creditProfile.getValidFor()!= null && creditProfile.getValidFor().getEndDateTimePatch() != null) )
                    .createdBy(aTelusChannel.getUserId(), creditProfile.getChannelPatch() != null)
                    .updatedBy(aTelusChannel.getUserId(), creditProfile.getChannelPatch() != null)
                    .originatorAppId(aTelusChannel.getOriginatorAppId(), creditProfile.getChannelPatch() != null)
                    .channelOrgId(aTelusChannel.getChannelOrgId(), creditProfile.getChannelPatch() != null)
                    .creditProfileStatusCd(creditProfile.getStatusCd(), creditProfile.getStatusCdPatch() != null)
                    .creditProfileStatusTs(creditProfile.getStatusTs(), creditProfile.getStatusTsPatch() != null)

                    
                    .creditClassCode(cryptoService.encryptOrNull(creditProfile.getCreditClassCd()), creditProfile.getCreditClassCdPatch() != null)
                    .creditDecisionCode(cryptoService.encryptOrNull(creditProfile.getCreditDecisionCd()), creditProfile.getCreditDecisionCdPatch() != null)
                    .riskLevelDecisionCode(cryptoService.encryptOrNull(creditProfile.getRiskLevelDecisionCd()), creditProfile.getRiskLevelDecisionCdPatch() != null)

                    .productCategoryBoltOn(creditProfile.getBoltonInd(), creditProfile.getBoltonIndPatch() != null)
                    ;


        } catch (Exception e) {
            throw new CreditException(HttpStatus.BAD_REQUEST, ERR_CODE_8001, "Failed in mapping to CreditProfileEntity", ERR_CODE_8001_MSG, (creditProfile != null) ? creditProfile.toString() : "");
        }

        PatchField<TimePeriod> validForPatch = creditProfile.getValidForPatch();
        if (validForPatch != null && !validForPatch.isValueNull()) {
            creditProfileEntity.validStartTs(DateTimeUtils.toUtcTimestamp(validForPatch.get().getStartDateTime()), validForPatch.get().getStartDateTimePatch() != null)
                    .validEndTs(DateTimeUtils.toUtcTimestamp(validForPatch.get().getEndDateTime()), validForPatch.get().getEndDateTimePatch() != null);
        }

        PatchField<List<TelusCharacteristic>> characteristicsPatch = creditProfile.getCharacteristicsPatch();
        if (characteristicsPatch != null && !characteristicsPatch.isValueNull()) {
            List<TelusCharacteristic> characteristicList = characteristicsPatch.get();
            creditProfileEntity.setPopulateMethodCd(getCharacteristicValue(characteristicList, CreditProfileConstant.POPULATE_METHOD_CD));
            creditProfileEntity.setCproflFormatCd(getCharacteristicValue(characteristicList, CreditProfileConstant.CPROFL_FORMAT_CD));
            creditProfileEntity.setCommentTxt(getCharacteristicValue(characteristicList, CreditProfileConstant.COMMENT_TXT));
            Timestamp busLastUpdtTs = DateTimeUtils.toUtcTimestamp(getCharacteristicValue(characteristicList, CreditProfileConstant.BUS_LAST_UPDT_TS));
            if(busLastUpdtTs!=null ) {
            	creditProfileEntity.setBusLastUpdtTs(busLastUpdtTs);
            }else{
            	creditProfileEntity.setBusLastUpdtTs(Timestamp.from(Instant.now()));
            }
            creditProfileEntity.setBypassMatchInd(Boolean.valueOf(getCharacteristicValue(characteristicList, CreditProfileConstant.BYPASS_MATCH_IND)));
        }

        if(CreditProfileConstant.WIRELINE_LINE_OF_BUSINESS.equals(creditProfile.getLineOfBusiness())) {
            PatchField<String> patchField = creditProfile.getBusLastUpdtTsPatch();
            if (Objects.nonNull(patchField) && !patchField.isValueNull()) {
                creditProfileEntity.setBusLastUpdtTs(DateTimeUtils.toUtcTimestamp(patchField.get()));
            }
        }

        return creditProfileEntity;
    }

    /**
     * Map CreditProfileEntity Entities to DTO
     *
     * @param entity
     * @return mapped DTO
     */
    //TODO the function needs to adapt to the newer CreditProfile without characterstics implementation
    public static TelusCreditProfile toDto(CreditProfileEntity entity) {
        TelusCreditProfile creditProfile = new TelusCreditProfile();
        creditProfile.setId(entity.getCreditProfileId());
        creditProfile.setCreationTs(DateTimeUtils.toUtcString(entity.getCreditProfileTs()));
        creditProfile.setPrimaryCreditScoreCd(entity.getCreditScore());
        creditProfile.setCreditRiskLevelNum(entity.getCreditRiskRating());

        if (entity.getValidEndTs() != null || entity.getValidStartTs() != null) {
            TimePeriod timePeriod = new TimePeriod();
            timePeriod.setStartDateTime(DateTimeUtils.toUtcString(entity.getValidStartTs()));
            timePeriod.setEndDateTime(DateTimeUtils.toUtcString(entity.getValidEndTs()));
            creditProfile.setValidFor(timePeriod);
        }

        TelusCreditProfileCharacteristic telusCharacteristic = new TelusCreditProfileCharacteristic();
        telusCharacteristic.setCreditProfileLegacyId(entity.getCreditProfileIdLegacy());
        telusCharacteristic.setPrimaryCreditScoreTypeCd(entity.getCreditScoreTypeCode());
        telusCharacteristic.setBureauDecisionCode(entity.getAgencyDecisionCode());
        telusCharacteristic.setCreditProgramName(entity.getCreditProgramName());
        telusCharacteristic.setCreditClassCd(entity.getCreditClassCode());
        telusCharacteristic.setCreditClassDate(DateTimeUtils.toUtcString(entity.getCreditClassTs()));
        telusCharacteristic.setCreditDecisionCd(entity.getCreditDecisionCode());
        telusCharacteristic.setCreditDecisionDate(DateTimeUtils.toUtcString(entity.getCreditDecisionTs()));
        telusCharacteristic.setRiskLevelDecisionCd(entity.getRiskLevelDecisionCode());
        telusCharacteristic.setRiskLevelDt(DateTimeUtils.toUtcString(entity.getRiskLevelTs()));
        telusCharacteristic.setClpRatePlanAmt(entity.getClpRatePlanAmt());
        telusCharacteristic.setClpContractTerm(entity.getClpContractTerm());
        telusCharacteristic.setClpCreditLimit(entity.getClpCreditLimitAmt());
        telusCharacteristic.setAverageSecurityDepositAmt(entity.getSecurityDepAmt());
        telusCharacteristic.setCreditAssessmentId(entity.getCreditAssessmentId());

        creditProfile.setTelusCharacteristic(telusCharacteristic);
        return creditProfile;
    }


    public static TelusCreditProfile toCreditProfileDto(CreditProfileEntity entity) {
        TelusCreditProfile creditProfile = new TelusCreditProfile();
        creditProfile.setId(entity.getCreditProfileId());
        
        creditProfile.setCreationTs(DateTimeUtils.toUtcString(entity.getCreditProfileTs()));
        creditProfile.setCreatedTs(DateTimeUtils.toUtcString(entity.getCreditProfileTs()));
        
        creditProfile.setStatusCd(entity.getCreditProfileStatusCd());
        creditProfile.setStatusTs(entity.getCreditProfileStatusTs());        
       
        creditProfile.setApplicationProvinceCd(entity.getApplicationSubProvCd());
        
        creditProfile.setPrimaryCreditScoreCd(entity.getCreditScore());
        creditProfile.setCreditRiskLevelNum(entity.getCreditRiskRating());

        if (entity.getValidEndTs() != null || entity.getValidStartTs() != null) {
            TimePeriod timePeriod = new TimePeriod();
            timePeriod.setStartDateTime(DateTimeUtils.toUtcString(entity.getValidStartTs()));
            timePeriod.setEndDateTime(DateTimeUtils.toUtcString(entity.getValidEndTs()));
            creditProfile.setValidFor(timePeriod);
        }

       creditProfile.setCreditProfileLegacyId(entity.getCreditProfileIdLegacy());
        creditProfile.setPrimaryCreditScoreTypeCd(entity.getCreditScoreTypeCode());
        creditProfile.setBureauDecisionCode(entity.getAgencyDecisionCode());
        creditProfile.setCreditProgramName(entity.getCreditProgramName());
        creditProfile.setCreditClassCd(entity.getCreditClassCode());
        creditProfile.setCreditClassDate(DateTimeUtils.toUtcString(entity.getCreditClassTs()));

        
        creditProfile.setCreditDecisionCd(entity.getCreditDecisionCode());
        creditProfile.setCreditDecisionDate(DateTimeUtils.toUtcString(entity.getCreditDecisionTs()));
        creditProfile.setRiskLevelDecisionCd(entity.getRiskLevelDecisionCode());
        creditProfile.setRiskLevelDt(DateTimeUtils.toUtcString(entity.getRiskLevelTs()));
        creditProfile.setClpRatePlanAmt(entity.getClpRatePlanAmt());
        creditProfile.setClpContractTerm(entity.getClpContractTerm());
        creditProfile.setClpCreditLimitAmt(entity.getClpCreditLimitAmt());
        creditProfile.setAverageSecurityDepositAmt(entity.getSecurityDepAmt());
        
        creditProfile.setRiskLevelRiskAssessment (new RiskLevelRiskAssessment());
        creditProfile.getRiskLevelRiskAssessment().setId(entity.getCreditAssessmentId());
        
        creditProfile.setCreditProfileConsentCd(entity.getCreditCheckConsentCd());
        creditProfile.setLineOfBusiness(entity.getLineOfBusiness());

        creditProfile.setBoltonInd(entity.getProductCategoryBoltOn());

        
        TelusChannel channel = new TelusChannel();
        channel.setUserId(entity.getCreatedBy());
        channel.setChannelOrgId(entity.getChannelOrgId());
        channel.setOriginatorAppId(entity.getOriginatorAppId());
        creditProfile.setChannel(channel);

        creditProfile.setUpdatedTs(DateTimeUtils.toUtcString(entity.getUpdatedTs()));

        
       
        
        
        
        ;
        // entity.getCommentTxt();
        List<TelusCharacteristic> cp_characteristics = new ArrayList<TelusCharacteristic>();
        if(entity.getCommentTxt()!=null) {
	        TelusCharacteristic commentTxtChar= new TelusCharacteristic();        
	        commentTxtChar.setName(CreditProfileConstant.COMMENT_TXT);
	        commentTxtChar.setValue(entity.getCommentTxt());
	        commentTxtChar.setValueType("String");
	        cp_characteristics.add(commentTxtChar);
        }
        
        if(entity.getBypassMatchInd()!=null) {
	        TelusCharacteristic bypassMatchIndChar= new TelusCharacteristic();        
	        bypassMatchIndChar.setName(CreditProfileConstant.BYPASS_MATCH_IND);
	        bypassMatchIndChar.setValue(entity.getBypassMatchInd()+"");
	        bypassMatchIndChar.setValueType("String");
	        cp_characteristics.add(bypassMatchIndChar);
        }
        
        if(entity.getPopulateMethodCd()!=null) {
	        TelusCharacteristic populateMethodCdChar= new TelusCharacteristic();        
	        populateMethodCdChar.setName(CreditProfileConstant.POPULATE_METHOD_CD);
	        populateMethodCdChar.setValue(entity.getPopulateMethodCd());
	        populateMethodCdChar.setValueType("String");
	        cp_characteristics.add(populateMethodCdChar);        
        }
        if(entity.getCproflFormatCd()!=null) {
	        TelusCharacteristic cproflFormatCdChar= new TelusCharacteristic();        
	        cproflFormatCdChar.setName(CreditProfileConstant.CPROFL_FORMAT_CD);
	        cproflFormatCdChar.setValue(entity.getCproflFormatCd());
	        cproflFormatCdChar.setValueType("String");
	        cp_characteristics.add(cproflFormatCdChar); 
        }
        if(entity.getBusLastUpdtTs()!=null) {
	        TelusCharacteristic busLastUpdtTsChar= new TelusCharacteristic();        
	        busLastUpdtTsChar.setName(CreditProfileConstant.BUS_LAST_UPDT_TS);
	        busLastUpdtTsChar.setValue(entity.getBusLastUpdtTs()+"");
	        busLastUpdtTsChar.setValueType("String");
	        cp_characteristics.add(busLastUpdtTsChar);
        }
        
		creditProfile.setCharacteristic(cp_characteristics);
		
        
        return creditProfile;
    }
}
