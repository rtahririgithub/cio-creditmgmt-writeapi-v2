package com.telus.credit.service.impl;

import static com.telus.credit.common.PdsRefConstants.ASSESSMENT_MSG_VALUE_CODE;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.common.CommonHelper;
import com.telus.credit.common.PdsRefConstants;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.model.Attachments;
import com.telus.credit.model.CreditProfileAuditDocument;
import com.telus.credit.model.Customer;
import com.telus.credit.model.Individual;
import com.telus.credit.model.Organization;
import com.telus.credit.model.OrganizationIdentification;
import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.RelatedPartyInterface;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.RiskLevelRiskAssessment;
import com.telus.credit.model.TelusCreditDecisionWarning;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.model.TelusCreditProfileCharacteristic;
import com.telus.credit.model.TelusIndividualIdentification;
import com.telus.credit.model.common.ApplicationConstants;
import com.telus.credit.model.common.PartyType;
import com.telus.credit.pds.model.Key;
import com.telus.credit.pds.model.MultiKeyReferenceDataItem;
import com.telus.credit.pds.service.MultiKeyReferenceDataService;
import com.telus.credit.pds.service.ReferenceDataService;

@Service
public class ResponseInterceptorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseInterceptorService.class);

    private CryptoService cryptoService;

    private ReferenceDataService referenceDataService;

    private AuditService auditService;

    private ObjectMapper objectMapper;

    public ResponseInterceptorService(CryptoService cryptoService,
                                      ReferenceDataService referenceDataService,
                                      AuditService auditService,
                                      ObjectMapper objectMapper) {
        this.cryptoService = cryptoService;
        this.referenceDataService = referenceDataService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    /**
     * Resolve field values which were not stored in DB. Keep encrypted values.
     * After that publish customer to audit topic
     *
     * @param customer
     * @param lang
     */
    public void resolveMissingFieldsAndAudit(TelusCreditProfile incomingCreditProfile,Customer customer) {
        if (customer == null) {
            return;
        }

        for (TelusCreditProfile cp : CommonHelper.nullSafe(customer.getCreditProfile())) {
            if (cp.getTelusCharacteristic() != null) {
                // for audit
                cp.getTelusCharacteristic().setRiskLevelNumber(cp.getCreditRiskLevelNum());
                cp.getTelusCharacteristic().setPrimaryCreditScoreCd(cp.getPrimaryCreditScoreCd());

                populateDecisionCode(cp.getTelusCharacteristic());
                populateAssessmentMessage(cp.getTelusCharacteristic());
            }
            else {
//                cp.setRiskLevelNumber(cp.getRiskLevelNumber());
//                cp.setPrimaryCreditScoreCd(cp.getCreditScore());
//                cp.setRelatedParties(cp.getRelatedParties());
//                cp.setAttachments(cp.getAttachments());
//                cp.setChannel(cp.getChannel());
//                cp.setRiskLevelRiskAssessment(cp.getRiskLevelRiskAssessment());
                populateDecisionCode(cp);
                populateAssessmentMessage(cp.getRiskLevelRiskAssessment());
            }
        }
		//STEP:publish change audit doc to be stored in firestore
        saveAudit(incomingCreditProfile,customer);
    }
    /**
     * Resolve fields values which were not stored in DB. Decrypt encrypted values
     *
     * @param customer
     */
    public void decryptCustomerFromDb(Customer customer) {
        if (customer == null) {
            return;
        }

        for (TelusCreditProfile cp : CommonHelper.nullSafe(customer.getCreditProfile())) {
            decryptCreditProfile(cp);
            //decryp[t engaged party's identifications
            List<RelatedParty> relatedParties = cp.getRelatedParties();
            if(relatedParties!=null) {
	            for (RelatedParty relatedParty : relatedParties) {
	            	if(relatedParty!=null) {
	            		decryptEngagedPartyPatch(relatedParty.getEngagedParty());
	            	}
				}
            }
            
            
            if (cp.getTelusCharacteristic() != null) {
                populateDecisionCode(cp.getTelusCharacteristic());
                populateAssessmentMessage(cp.getTelusCharacteristic());
            }
            else {
                populateDecisionCode(cp);
                populateAssessmentMessage(cp.getRiskLevelRiskAssessment());
            }
        }
    }
    
    public void decryptCreditProfileFromDb(TelusCreditProfile cp) {
        if (cp == null) {
            return;
        }

            decryptCreditProfile(cp);
            //decryp[t engaged party's identifications
            List<RelatedParty> relatedParties = cp.getRelatedParties();
            if(relatedParties!=null) {
	            for (RelatedParty relatedParty : relatedParties) {
	            	if(relatedParty!=null) {
	            		decryptEngagedPartyPatch(relatedParty.getEngagedParty());
	            	}
				}
            }
            
            
            if (cp.getTelusCharacteristic() != null) {
                populateDecisionCode(cp.getTelusCharacteristic());
                populateAssessmentMessage(cp.getTelusCharacteristic());
            }
            else {
                populateDecisionCode(cp);
                populateAssessmentMessage(cp.getRiskLevelRiskAssessment());
            }
    }   
	/*
	 * public void resolveMissingFieldst(Customer customer, String lang) { if
	 * (customer == null) { return; }
	 * 
	 * for (TelusCreditProfile cp :
	 * CommonHelper.nullSafe(customer.getCreditProfile())) { if
	 * (cp.getTelusCharacteristic() != null) { // for audit
	 * cp.getTelusCharacteristic().setRiskLevelNumber(cp.getCreditRiskLevelNum());
	 * cp.getTelusCharacteristic().setPrimaryCreditScoreCd(cp.
	 * getPrimaryCreditScoreCd());
	 * 
	 * populateDecisionCode(cp.getTelusCharacteristic());
	 * populateAssessmentMessage(cp.getTelusCharacteristic()); } else { //
	 * cp.setRiskLevelNumber(cp.getRiskLevelNumber()); //
	 * cp.setPrimaryCreditScoreCd(cp.getCreditScore()); //
	 * cp.setRelatedParties(cp.getRelatedParties()); //
	 * cp.setAttachments(cp.getAttachments()); // cp.setChannel(cp.getChannel()); //
	 * cp.setRiskLevelRiskAssessment(cp.getRiskLevelRiskAssessment());
	 * populateDecisionCode(cp);
	 * populateAssessmentMessage(cp.getRiskLevelRiskAssessment()); } }
	 * 
	 * }
	 */
    /**
     * Publish customer to audit topic
     *
     * @param customer
     */
    public void saveAudit( TelusCreditProfile incomingCreditProfile, Customer updateCustomer) {
        CreditProfileAuditDocument auditDocument = AuditService.auditContext();
        try {
        	auditDocument.setInputRequest(objectMapper.writeValueAsString(incomingCreditProfile));
            auditDocument.setResponseData(objectMapper.writeValueAsString(updateCustomer));
            auditService.addAuditLog(auditDocument);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot write audit log, customer " + updateCustomer.getId(), e);
        }
    }



    private void decryptCreditProfile(TelusCreditProfile creditProfile) {
        if (creditProfile == null) {
            return;
        }

        long start = System.currentTimeMillis();

        String score  = cryptoService.decryptAndIgnoreError(creditProfile.getPrimaryCreditScoreCd());
        String rating = cryptoService.decryptAndIgnoreError(creditProfile.getCreditRiskLevelNum());
        List<Attachments> attachments = CommonHelper.nullSafe(creditProfile.getAttachments()).stream()
                .map(attachment -> {
                    attachment.setContent(cryptoService.decryptAndIgnoreError(attachment.getContent()));
                    return attachment;
                }).collect(Collectors.toList());
        creditProfile.setAttachments(attachments);

        if (creditProfile.getTelusCharacteristic() != null) {
            TelusCreditProfileCharacteristic characteristic = creditProfile.getTelusCharacteristic();
            characteristic.setPrimaryCreditScoreCd(StringUtils.trimToNull(score));
            characteristic.setRiskLevelNumber(StringUtils.trimToNull(rating));

            characteristic.setCreditClassCd(cryptoService.decryptAndIgnoreError(characteristic.getCreditClassCd()));
            characteristic.setCreditDecisionCd(cryptoService.decryptAndIgnoreError(characteristic.getCreditDecisionCd()));
            characteristic.setRiskLevelDecisionCd(cryptoService.decryptAndIgnoreError(characteristic.getRiskLevelDecisionCd()));

            for (TelusCreditDecisionWarning warning : CommonHelper.nullSafe(characteristic.getWarningHistoryList())) {
                decrypt(warning);
            }
        }
        else {
            creditProfile.setPrimaryCreditScoreCd(StringUtils.trimToNull(score));
            creditProfile.setCreditRiskLevelNum(StringUtils.trimToNull(rating));

            creditProfile.setCreditClassCd(cryptoService.decryptAndIgnoreError(creditProfile.getCreditClassCd()));
            creditProfile.setCreditDecisionCd(cryptoService.decryptAndIgnoreError(creditProfile.getCreditDecisionCd()));
            creditProfile.setRiskLevelDecisionCd(cryptoService.decryptAndIgnoreError(creditProfile.getRiskLevelDecisionCd()));
            // Get only non expired warnings!
            List<TelusCreditDecisionWarning> warnings = CommonHelper.nullSafe(creditProfile.getWarningHistoryList()).stream()
                    .filter(warn -> ObjectUtils.isEmpty(warn.getValidFor()) || StringUtils.isEmpty(warn.getValidFor().getEndDateTime())).collect(Collectors.toList());
            creditProfile.setWarningHistoryList(warnings);
            for (TelusCreditDecisionWarning warning : warnings) {
                decrypt(warning);
            }
        }

		/*
		 * RelatedPartyToPatch x =
		 * creditProfile.getRelatedParties().get(0).getEngagedParty(); x.get
		 * PatchField<List<TelusIndividualIdentification>> engagedParty =
		 * creditProfile.getRelatedParties().get(0).getEngagedParty().
		 * getIndividualIdentificationPatch(); if( !engagedParty.isValueNull() ) {
		 * List<TelusIndividualIdentification> individualIdentifications =
		 * engagedParty.get(); for (TelusIndividualIdentification
		 * telusIndividualIdentification : individualIdentifications) {
		 * decryptEngagedParty(telusIndividualIdentification);
		 * 
		 * } }
		 */
        
        LOGGER.debug("Decrypt credit profile took {}ms", System.currentTimeMillis() - start);
    }

    private void decrypt(TelusCreditDecisionWarning decisionWarning) {
        decisionWarning.setWarningStatusCd(cryptoService.decryptAndIgnoreError(decisionWarning.getWarningStatusCd()));
        decisionWarning.setWarningCd(cryptoService.decryptAndIgnoreError(decisionWarning.getWarningCd()));
        decisionWarning.setWarningCategoryCd(cryptoService.decryptAndIgnoreError(decisionWarning.getWarningCategoryCd()));
        decisionWarning.setWarningTypeCd(cryptoService.decryptAndIgnoreError(decisionWarning.getWarningTypeCd()));
        decisionWarning.setWarningItemTypeCd(cryptoService.decryptAndIgnoreError(decisionWarning.getWarningItemTypeCd()));
    }

    private void decryptEngagedParty(RelatedPartyInterface engagedParty) {
        if (engagedParty == null) {
            return;
        }

        long start = System.currentTimeMillis();

        if (PartyType.INDIVIDUAL.equals(engagedParty.getRelatedPartyType())) {
            List<TelusIndividualIdentification> identifications = ((Individual) engagedParty).getIndividualIdentification();
            for (TelusIndividualIdentification identification : CommonHelper.nullSafe(identifications)) {
                identification.setIdentificationId(cryptoService.decryptAndIgnoreError(identification.getIdentificationId()));
            }
        } else {
            List<OrganizationIdentification> identifications = ((Organization) engagedParty).getOrganizationIdentification();
            for (OrganizationIdentification identification : CommonHelper.nullSafe(identifications)) {
                identification.setIdentificationId(cryptoService.decryptAndIgnoreError(identification.getIdentificationId()));
            }
        }
        LOGGER.debug("Decrypt party took {}ms", System.currentTimeMillis() - start);
    }

    private void decryptEngagedPartyPatch (RelatedPartyToPatch engagedParty) {
        if (engagedParty == null) {
            return;
        }

        long start = System.currentTimeMillis();

      if (engagedParty!=null && engagedParty.getIndividualIdentification() !=null) 
        {
            List<TelusIndividualIdentification> identifications = engagedParty.getIndividualIdentification();
            for (TelusIndividualIdentification identification : CommonHelper.nullSafe(identifications)) {
                identification.setIdentificationId(cryptoService.decryptAndIgnoreError(identification.getIdentificationId()));
            }
        } 
        
      if (engagedParty!=null && engagedParty.getOrganizationIdentification() !=null)  
        {
            List<OrganizationIdentification> identifications = engagedParty.getOrganizationIdentification();
            for (OrganizationIdentification identification : CommonHelper.nullSafe(identifications)) {
                identification.setIdentificationId(cryptoService.decryptAndIgnoreError(identification.getIdentificationId()));
            }
        }
        LOGGER.debug("Decrypt party took {}ms", System.currentTimeMillis() - start);
    }    
	/**
	 * Resolve BureauDecisionMessage from BureauDecisionCode
	 * 
	 * @param characteristic
	 */
	private void populateDecisionCode(TelusCreditProfileCharacteristic characteristic) {
        String decisionCode = characteristic.getBureauDecisionCode();
		if (StringUtils.isNotBlank(decisionCode)) {
            List<Key> keys = MultiKeyReferenceDataService.createKeyList(
                    PdsRefConstants.BUREAU_DECISION_CODE, decisionCode);
            MultiKeyReferenceDataItem data = referenceDataService.getCreditDecisionRule(keys);
            if (data == null)
            {
              characteristic.setBureauDecisionMessage(decisionCode);
              characteristic.setBureauDecisionMessage_fr(decisionCode);
      	      LOGGER.warn("The bureau decision message not found in refpd data for " + decisionCode );
      		
            }
            else
            {
            data.getValues().forEach(value -> {
                if (PdsRefConstants.ENG_MESSAGE.equalsIgnoreCase(value.getValueCode())) {
                    characteristic.setBureauDecisionMessage(value.getValue());
                } else if (PdsRefConstants.FR_MESSAGE.equalsIgnoreCase(value.getValueCode())) {
                    characteristic.setBureauDecisionMessage_fr(value.getValue());
                	}
            	});
            }
        }
    }

    private void populateDecisionCode(TelusCreditProfile creditProfile) {
        String decisionCode = creditProfile.getBureauDecisionCode();
        if (StringUtils.isNotBlank(decisionCode)) {
            List<Key> keys = MultiKeyReferenceDataService.createKeyList(
                    PdsRefConstants.BUREAU_DECISION_CODE, decisionCode);
            MultiKeyReferenceDataItem data = referenceDataService.getCreditDecisionRule(keys);
            if (data == null)
            {
                creditProfile.setBureauDecisionMessage(decisionCode);
                creditProfile.setBureauDecisionMessage_fr(decisionCode);
                LOGGER.warn( "The bureau decision message not found in refpd data for " + decisionCode );

            }
            else
            {
                data.getValues().forEach(value -> {
                    if (PdsRefConstants.ENG_MESSAGE.equalsIgnoreCase(value.getValueCode())) {
                        creditProfile.setBureauDecisionMessage(value.getValue());
                    } else if (PdsRefConstants.FR_MESSAGE.equalsIgnoreCase(value.getValueCode())) {
                        creditProfile.setBureauDecisionMessage_fr(value.getValue());
                    }
                });
            }
        }
    }

    private void populateAssessmentMessage(RiskLevelRiskAssessment riskLevelRiskAssessment) {
        if (ObjectUtils.isNotEmpty(riskLevelRiskAssessment)) {
          //  Optional<String> optional = Optional.ofNullable(riskLevelRiskAssessment.getAssessmentMessageCd());            
            String messageCode = riskLevelRiskAssessment.getAssessmentMessageCd();
            if (StringUtils.isNotBlank(messageCode)) {
                List<Key> keys = MultiKeyReferenceDataService.createKeyList(
                        PdsRefConstants.MESSAGE_KEY, messageCode);
                MultiKeyReferenceDataItem data = referenceDataService.getAssessmentMessage(keys);

                if (data == null) {
                    riskLevelRiskAssessment.setAssessmentMessageTxtEn(messageCode);
                    riskLevelRiskAssessment.setAssessmentMessageTxtFr(messageCode);
                    LOGGER.warn("The assessment message not found in refpd data for " + messageCode);
                } else {
                    try {

                        data.getValues().forEach(value -> {
                            if (ASSESSMENT_MSG_VALUE_CODE.equals(value.getValueCode())) {
                                if (ApplicationConstants.EN_LANG.equalsIgnoreCase(value.getLangCode())) {
                                    riskLevelRiskAssessment.setAssessmentMessageTxtEn(value.getValue());
                                }
                                if (ApplicationConstants.FR_LANG.equalsIgnoreCase(value.getLangCode())) {
                                    riskLevelRiskAssessment.setAssessmentMessageTxtFr(value.getValue());
                                }
                            }
                        });
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn(" Ignoring invalid key for assessment:{}", e.getMessage());
                    }
                }
            }
        }
    }

	/**
	 * Resolve assessment message from AssessmentMessageCode
	 * 
	 * @param characteristic
	 */
	private void populateAssessmentMessage(TelusCreditProfileCharacteristic characteristic) {
        String messageCode = characteristic.getAssessmentMessageCode();
		if (StringUtils.isNotBlank(messageCode)) {
            List<Key> keys = MultiKeyReferenceDataService.createKeyList(
                    PdsRefConstants.MESSAGE_KEY, messageCode);
            MultiKeyReferenceDataItem data = referenceDataService.getAssessmentMessage(keys);
            
            if (data == null)
            		{
            	      characteristic.setAssessmentMessage(messageCode);
            	      characteristic.setAssessmentMessage_fr(messageCode);
            	      LOGGER.warn("The assessment message not found in refpd data for " + messageCode );
            		}
            else
            {
            try {
               
                data.getValues().forEach(value -> {
                    if (ASSESSMENT_MSG_VALUE_CODE.equals(value.getValueCode())) {
                        if (ApplicationConstants.EN_LANG.equalsIgnoreCase(value.getLangCode())) {
                            characteristic.setAssessmentMessage(value.getValue());
                        } if (ApplicationConstants.FR_LANG.equalsIgnoreCase(value.getLangCode())) {
                            characteristic.setAssessmentMessage_fr(value.getValue());
                        	}
                    	}
                	});
            	} catch (IllegalArgumentException e) {
            	LOGGER.warn( "Ignoring invalid key for assessment:{}", ExceptionHelper.getStackTrace(e));
            	}
            }
        }
    }
}
