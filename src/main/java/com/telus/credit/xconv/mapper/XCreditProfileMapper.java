package com.telus.credit.xconv.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.telus.credit.common.CommonHelper;
import com.telus.credit.common.CreditProfileConstant;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.dao.entity.CreditProfileEntity;
import com.telus.credit.dao.entity.PartyContactMediumEntity;
import com.telus.credit.dao.entity.XCreditProfileEntity;
import com.telus.credit.exceptions.CryptoException;
import com.telus.credit.model.Individual;
import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCharacteristic;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.model.TimePeriod;
import com.telus.credit.model.common.PartyType;

@Component
public class XCreditProfileMapper {

    private static CryptoService cryptoService;

    @Autowired
    private void setCryptoService(CryptoService cryptoService) {
        XCreditProfileMapper.cryptoService = cryptoService;
    }

    public static TelusCreditProfile toTelusCreditProfile(XCreditProfileEntity xCP_entity) throws Exception {

    	TelusCreditProfile creditProfile = new TelusCreditProfile();
        creditProfile.setId(xCP_entity.getCreditProfileId() );
        creditProfile.setCreatedTs(DateTimeUtils.toUtcString(xCP_entity.getCreditProfileTs()));
        creditProfile.setCreatedBy(xCP_entity.getCreatedBy());
        creditProfile.setUpdatedTs(DateTimeUtils.toUtcString(xCP_entity.getCreditProfileTs()));

	    creditProfile.setPrimaryCreditScoreCd(xCP_entity.getCreditScore());
	    creditProfile.setCreditRiskLevelNum(xCP_entity.getCreditRiskRating());
	    creditProfile.setCreditRiskLevelNum(xCP_entity.getRiskLevelDecisionCode());
	    creditProfile.setCreditDecisionCd(xCP_entity.getCreditDecisionCode());
	    creditProfile.setCreditClassCd(xCP_entity.getCreditClassCode());

		TimePeriod timePeriod = new TimePeriod();
		timePeriod.setStartDateTime(DateTimeUtils.toUtcString(xCP_entity.getValidStartTs()));
		timePeriod.setEndDateTime(DateTimeUtils.toUtcString(xCP_entity.getValidEndTs()));
        creditProfile.setValidFor(timePeriod);

        creditProfile.setCreditProfileLegacyId(xCP_entity.getCreditProfileIdLegacy());
        creditProfile.setPrimaryCreditScoreTypeCd(xCP_entity.getCreditScoreTypeCode());
        creditProfile.setBureauDecisionCode(xCP_entity.getAgencyDecisionCode());
        creditProfile.setCreditProgramName(xCP_entity.getCreditProgramName());
        creditProfile.setCreditClassDate(DateTimeUtils.toUtcString(xCP_entity.getCreditClassTs())); 
        creditProfile.setCreditDecisionDate(DateTimeUtils.toUtcString(xCP_entity.getCreditDecisionTs()));
        creditProfile.setRiskLevelDt(DateTimeUtils.toUtcString(xCP_entity.getRiskLevelTs()));
        creditProfile.setClpRatePlanAmt(xCP_entity.getClpRatePlanAmt());
        creditProfile.setClpContractTerm(xCP_entity.getClpContractTerm());
        creditProfile.setClpCreditLimitAmt(parseDouble(xCP_entity.getClpCreditLimitAmt()));
        creditProfile.setAverageSecurityDepositAmt(parseDouble(xCP_entity.getSecurityDepAmt()));        
        creditProfile.setCreditAssessmentId((Long) CommonHelper.safeTransform(xCP_entity.getCreditAssessmentId(), v -> Long.parseLong(StringUtils.trim(v)), null, null));

        creditProfile.setApplicationProvinceCd(xCP_entity.getApplicationSubProvCd());
        creditProfile.setCreditProfileConsentCd(xCP_entity.getCredCheckConsentCd());
        creditProfile.setLineOfBusiness(xCP_entity.getLineOfBusiness());
        creditProfile.setStatusCd(xCP_entity.getStatus());
 
//cp_characteristics
        List<TelusCharacteristic> cp_characteristics = new ArrayList<TelusCharacteristic>();
        if(xCP_entity.getBypassMatchInd()!=null) {
	        TelusCharacteristic bypassMatchIndChar= new TelusCharacteristic();        
	        bypassMatchIndChar.setName(CreditProfileConstant.BYPASS_MATCH_IND);
	        bypassMatchIndChar.setValue(xCP_entity.getBypassMatchInd()+"");
	        bypassMatchIndChar.setValueType("String");
	        cp_characteristics.add(bypassMatchIndChar);
        }

        if(xCP_entity.getPopulateMethodCd()!=null) {
	        TelusCharacteristic populateMethodCdChar= new TelusCharacteristic();        
	        populateMethodCdChar.setName(CreditProfileConstant.POPULATE_METHOD_CD);
	        populateMethodCdChar.setValue(xCP_entity.getPopulateMethodCd());
	        populateMethodCdChar.setValueType("String");
	        cp_characteristics.add(populateMethodCdChar);        
        }
        
        if(xCP_entity.getCproflFormatCd()!=null) {
	        TelusCharacteristic cproflFormatCdChar= new TelusCharacteristic();        
	        cproflFormatCdChar.setName(CreditProfileConstant.CPROFL_FORMAT_CD);
	        cproflFormatCdChar.setValue(xCP_entity.getCproflFormatCd());
	        cproflFormatCdChar.setValueType("String");
	        cp_characteristics.add(cproflFormatCdChar); 
        }
        if(xCP_entity.getBusLastUpdtTs()!=null) {
	        TelusCharacteristic busLastUpdtTsChar= new TelusCharacteristic();        
	        busLastUpdtTsChar.setName(CreditProfileConstant.BUS_LAST_UPDT_TS);
	        busLastUpdtTsChar.setValue(xCP_entity.getBusLastUpdtTs()+"");
	        busLastUpdtTsChar.setValueType("String");
	        cp_characteristics.add(busLastUpdtTsChar);
        }
        
        if(xCP_entity.getCommentTxt()!=null) {
	    	TelusCharacteristic busLastUpdtTsChar = new TelusCharacteristic();
	    	busLastUpdtTsChar.setName(CreditProfileConstant.COMMENT_TXT);
	    	busLastUpdtTsChar.setValue(xCP_entity.getCommentTxt());
	    	busLastUpdtTsChar.setValueType("String");		
			cp_characteristics.add(busLastUpdtTsChar);	
        }
        creditProfile.setCharacteristic(cp_characteristics);
        
//TelusChannel        
        TelusChannel channel = new TelusChannel();
        channel.setUserId(xCP_entity.getCreatedBy());
        channel.setChannelOrgId(xCP_entity.getChannelOrgId());
        channel.setOriginatorAppId(xCP_entity.getOriginatorAppId());
        creditProfile.setChannel(channel);                
        creditProfile.setChannel(channel);
        creditProfile.setBoltonInd(xCP_entity.getProductCategoryBoltOn());
        

     // populated relatedParty for owner customer         
    	RelatedParty customerRelatedParty = new RelatedParty();
    	customerRelatedParty.setId(xCP_entity.getCustomerId()+"");
    	customerRelatedParty.setRole("Customer");
    	customerRelatedParty.setType("Customer");
        creditProfile.setRelatedParties(Collections.singletonList(customerRelatedParty));
        
       RelatedPartyToPatch aEngagedParty = XEngagedPartyMapper.mapEngagedParty(xCP_entity);
       customerRelatedParty.setEngagedParty(aEngagedParty);

        return creditProfile;
    }    
    public static CreditProfileEntity toCreditProfileEntity(XCreditProfileEntity entity) {

        CreditProfileEntity creditProfile = new CreditProfileEntity();
        creditProfile.creditProfileId(entity.getCreditProfileId());
        creditProfile.creditProfileTs(entity.getCreditProfileTs());
        try {
            creditProfile.setCreditScore(cryptoService.encryptOrNull(entity.getCreditScore()));
            creditProfile.setCreditRiskRating(cryptoService.encryptOrNull(entity.getCreditRiskRating()));
            creditProfile.setRiskLevelDecisionCode(cryptoService.encryptOrNull(entity.getRiskLevelDecisionCode()));
            creditProfile.setCreditDecisionCode(cryptoService.encryptOrNull(entity.getCreditDecisionCode()));
            creditProfile.setCreditClassCode(cryptoService.encryptOrNull(entity.getCreditClassCode()));
        } catch (Exception e) {
            throw new CryptoException(e);
        }

        creditProfile.setValidStartTs(entity.getValidStartTs());
        creditProfile.setValidEndTs(entity.getValidEndTs());

        creditProfile.setCreditProfileIdLegacy(entity.getCreditProfileIdLegacy());
        creditProfile.setCreditScoreTypeCode(entity.getCreditScoreTypeCode());
        creditProfile.setAgencyDecisionCode(entity.getAgencyDecisionCode());
        creditProfile.setCreditProgramName(entity.getCreditProgramName());
        creditProfile.setCreditClassTs(entity.getCreditClassTs());
        creditProfile.setCreditDecisionTs(entity.getCreditDecisionTs());
        creditProfile.setRiskLevelTs(entity.getRiskLevelTs());
        creditProfile.setClpRatePlanAmt(entity.getClpRatePlanAmt());
        creditProfile.setClpContractTerm(entity.getClpContractTerm());
        creditProfile.setClpCreditLimitAmt(parseDouble(entity.getClpCreditLimitAmt()));
        creditProfile.setSecurityDepAmt(parseDouble(entity.getSecurityDepAmt()));
        creditProfile.setCreditAssessmentId((Long) CommonHelper.safeTransform(entity.getCreditAssessmentId(), v -> Long.parseLong(StringUtils.trim(v)), null, null));

        creditProfile.setBypassMatchInd(entity.getBypassMatchInd());
        creditProfile.setApplicationSubProvCd(entity.getApplicationSubProvCd());
        creditProfile.setCreditCheckConsentCd(entity.getCredCheckConsentCd());
        creditProfile.setLineOfBusiness(entity.getLineOfBusiness());
        creditProfile.setCreditProfileStatusCd(entity.getStatus());
        
        creditProfile.setPopulateMethodCd(entity.getPopulateMethodCd());
        creditProfile.setCproflFormatCd(entity.getCproflFormatCd());
        creditProfile.setCommentTxt(entity.getCommentTxt());
        creditProfile.setBusLastUpdtTs(entity.getBusLastUpdtTs());
        creditProfile.setProductCategoryBoltOn(entity.getProductCategoryBoltOn());
        
        creditProfile.setOriginatorAppId(entity.getOriginatorAppId());
        creditProfile.setChannelOrgId(entity.getChannelOrgId());
        creditProfile.setCreatedBy(entity.getCreatedBy());
        creditProfile.setCreatedTs(entity.getCreatedTs());
        creditProfile.setUpdatedBy(entity.getUpdatedBy());
        creditProfile.setUpdatedTs(entity.getUpdatedTs());
        
        return creditProfile;
    }

    public static PartyContactMediumEntity mapContactMedium(XCreditProfileEntity xcpe) {
        PartyContactMediumEntity entity = new PartyContactMediumEntity()
                .mediumType(xcpe.getMediumType())
                .preffered((Boolean) CommonHelper.safeTransform(xcpe.getPreffered(), v-> BooleanUtils.toBoolean(StringUtils.trim(v)), null, null))
                .city(xcpe.getCity())
                .contactType(xcpe.getContactType())
                .countryCode(xcpe.getCountryCode())
                .postCode(xcpe.getPostCode())
                .stateProvinceCode(xcpe.getStateProvinceCode())
                .street1(xcpe.getStreet1())
                .street2(xcpe.getStreet2())
                .street3(xcpe.getStreet3())
                .street4(xcpe.getStreet4())
                .street5(xcpe.getStreet5())
                .validStartTs(xcpe.getValidStartTs())
                .validEndTs(xcpe.getValidEndTs())
                .originatorAppId(xcpe.getOriginatorAppId())
                .channelOrgId(xcpe.getChannelOrgId())
                .createdBy(xcpe.getCreatedBy())
                .createdTs(xcpe.getCreatedTs())
                .updatedBy(xcpe.getUpdatedBy())
                .updatedTs(xcpe.getUpdatedTs());

        return entity;
    }

    private static BigDecimal parseDouble(String value) {
        value = StringUtils.trimToNull(value);
        return (BigDecimal) CommonHelper.safeTransform(value, BigDecimal::new, null, null);
    }
}
