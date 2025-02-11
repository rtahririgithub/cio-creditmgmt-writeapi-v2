package com.telus.credit.service.impl;

import com.telus.credit.common.CommonHelper;
import com.telus.credit.common.CreditMgmtCommonConstants;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.common.ErrorCode;
import com.telus.credit.common.RequestContext;
import com.telus.credit.config.CreditWorthinessConfig;
import com.telus.credit.dao.CreditProfileDao;
import com.telus.credit.dao.CreditWarningHistoryDao;
import com.telus.credit.dao.CustomerCreditProfileRelDao;
import com.telus.credit.dao.ProdQualDao;
import com.telus.credit.dao.entity.CreditProfileEntity;
import com.telus.credit.dao.entity.CreditWarningHistoryEntity;
import com.telus.credit.dao.entity.CustomerCreditProfileRelEntity;
import com.telus.credit.dao.entity.CustomerEntity;
import com.telus.credit.dao.entity.ProdQualEntity;
import com.telus.credit.dao.mapper.CompositeRowMapper.CompositeEntity;
import com.telus.credit.exceptions.CreditException;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.model.AccountInfo;
import com.telus.credit.model.Customer;
import com.telus.credit.model.ProductCategoryQualification;
import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCreditDecisionWarning;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.model.mapper.CreditWarningHistoryModelMapper;
import com.telus.credit.model.mapper.ProdQualModelMapper;
import com.telus.credit.model.mapper.TelusCreditProfileModelMapper;
import com.telus.credit.pubsub.service.TelusMDMEventSender;
import com.telus.credit.service.CreditProfileService;
import com.telus.credit.service.CustomerService;
import com.telus.credit.service.EngagedPartyService;
import com.telus.credit.service.ValidationService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Service


public class TelusCreditProfileService implements CreditProfileService<TelusCreditProfile> {

	
	
    private static final Logger LOGGER = LoggerFactory.getLogger(TelusCreditProfileService.class);

    private final CreditProfileDao creditProfileDao;

    private final CreditWarningHistoryDao creditWarningHistoryDao;

    private final ProdQualDao prodQualDao;

    private final CustomerCreditProfileRelDao customerCreditProfileRelDao;

    private final ValidationService validationService;
    
	@Autowired
	private EngagedPartyService engagedPartyService;

    private final CustomerService customerService;
    private final TelusMDMEventSender mdmEventSender;
   // private final WLNCreditProfileManagementProxyServicePortType servicePortType;
    private final CreditWorthinessConfig creditWorthinessConfig;

    public TelusCreditProfileService(CreditProfileDao creditProfileDao,
                                     CreditWarningHistoryDao creditWarningHistoryDao,
                                     CustomerCreditProfileRelDao customerCreditProfileRelDao,
                                     ValidationService validationService,
                                     @Lazy CustomerService customerService,
                                     PlatformTransactionManager txManager,
                                     ProdQualDao prodQualDao,
                                     TelusMDMEventSender mdmEventSender,
                                    // WLNCreditProfileManagementProxyServicePortType servicePortType,
                                     CreditWorthinessConfig creditWorthinessConfig) {

        this.creditProfileDao = creditProfileDao;
        this.creditWarningHistoryDao = creditWarningHistoryDao;
        this.customerCreditProfileRelDao = customerCreditProfileRelDao;
        this.validationService = validationService;
        this.customerService = customerService;
        this.mdmEventSender = mdmEventSender;
        this.prodQualDao = prodQualDao;
      //  this.servicePortType = servicePortType;
        this.creditWorthinessConfig = creditWorthinessConfig;
    }

    /**
     * Create new credit profiles for a customer uid. This method doesn't do any update
     *
     * @param customerUid
     * @param creditProfile
     */
   
    @Transactional
    @Override
    public void createCreditProfile(String customerUid, TelusCreditProfile creditProfile) {
    	
    	TelusChannel aTelusChannel= creditProfile.getChannel();
    	if(aTelusChannel==null) {
    		aTelusChannel= new TelusChannel();
    	}  	
        validationService.validateForCreate(creditProfile);       
       if (  creditProfile.getCreatedTs()== null || creditProfile.getCreatedTs().isEmpty()) {
		creditProfile.setCreatedTs(DateTimeUtils.toUtcString(new Date()));
		creditProfile.setCreationTs(creditProfile.getCreatedTs());
       }
        LOGGER.info("Creating new credit profile");
        final String profileId = creditProfileDao.insert(TelusCreditProfileModelMapper.toCreditProfileEntity(creditProfile)
                .createdBy(aTelusChannel.getUserId())
                .updatedBy(aTelusChannel.getUserId())
                .originatorAppId(aTelusChannel.getOriginatorAppId())
                .channelOrgId(aTelusChannel.getChannelOrgId()));

        customerCreditProfileRelDao.insert(new CustomerCreditProfileRelEntity()
                .creditProfileId(profileId)
                .creditProfileCustomerId(customerUid)
                .createdBy(aTelusChannel.getUserId())
                .updatedBy(aTelusChannel.getUserId())
                .originatorAppId(aTelusChannel.getOriginatorAppId())
                .channelOrgId(aTelusChannel.getChannelOrgId()));

        creditProfile.setId(profileId);

        
        // TelusCreditProfileCharacteristic telusCharacteristic1 = creditProfile.getTelusCharacteristic();
        // saveCreditWarnings(profileId, telusCharacteristic.getWarningHistoryList(), aTelusChannel);
        saveCreditWarnings(profileId, creditProfile.getWarningHistoryList(), aTelusChannel,creditProfile.getLineOfBusiness());
    }

    @Transactional
    @Override
    @Primary

    
    
    public Customer createCreditProfileWithoutId(RequestContext requestContext, TelusCreditProfile incomingCreditProfile,
                                             AccountInfo accountInfo, long receivedTime,
                                             long submitterEventTime, String eventDescription) {
    	
    	 validationService.validateForCreate(incomingCreditProfile);
    	 
    	long relatedPartyCustomerRoleCustId = incomingCreditProfile.getRelatedPartyCustomerRoleCustId();
    	//check if customer already exist in sql  db
    	Customer customer = customerService.getCustomerPartyFromDatabase(relatedPartyCustomerRoleCustId);   
    	String requestLineOfBusiness = (incomingCreditProfile.getLineOfBusiness()!=null)?incomingCreditProfile.getLineOfBusiness():"";
    	List<TelusCreditProfile> dbCreditProfileList = customer.getCreditProfile();
    	List<TelusCreditProfile> filtereByLOB_DbCreditProfileListList = dbCreditProfileList.stream()
	            .filter(entity -> entity.getLineOfBusiness().equals(requestLineOfBusiness))
	            .collect(Collectors.toList());    	
        if (filtereByLOB_DbCreditProfileListList!=null && !filtereByLOB_DbCreditProfileListList.isEmpty()) {
            List<String> customerIds = incomingCreditProfile.getRelatedParties().stream().map(RelatedParty::getId).collect(Collectors.toList());
            if (customerIds.size() >= 1) {
                LOGGER.info("Credit profile already exists for cust id {}", customerIds.get(0));
                //Customer customer = customerService.getCustomerFromDatabase(Long.parseLong(customerIds.get(0)));
                incomingCreditProfile.setId(customer.getCreditProfile().get(0).getId());

                incomingCreditProfile = customer.getCreditProfile().get(0);
                return customer;
            }
        }
        
        if (incomingCreditProfile.getCreatedTs() == null || incomingCreditProfile.getCreatedTs().isEmpty()) {
            incomingCreditProfile.setCreatedTs(DateTimeUtils.toUtcString(new Date()));
        }
        if (incomingCreditProfile.getCreationTs() == null || incomingCreditProfile.getCreationTs().isEmpty()) {
            incomingCreditProfile.setCreationTs(DateTimeUtils.toUtcString(new Date()));
        }        
        LOGGER.info("Creating new credit profile");
        incomingCreditProfile.setCustomerCreditProfileRelCd(CreditMgmtCommonConstants.PRIMARY_CREDITPROFILE);
        incomingCreditProfile.setStatusCd("A");
        incomingCreditProfile.setStatusTs(""+DateTimeUtils.toUtcTimestamp(incomingCreditProfile.getCreatedTs()));        
        

        
		Customer cust = customerService.createCreditProfileResource(requestContext, relatedPartyCustomerRoleCustId, incomingCreditProfile, accountInfo, receivedTime, submitterEventTime, eventDescription);        
        
		 LOGGER.info("cust="+ cust);
		 return cust;

    }


    /**
     * Update credit profile belonging to a specific customer uid. If credit profile doesn't exist (null id) it will be created.
     * If credit profile is from ten pubsub, it will update the most recently updated credit profile
     *
     * @param customerEntity
     * @param telusCreditProfileToPatch
     */
    @Transactional
    @Override
    public void patchCreditProfileByCustId(CustomerEntity customerEntity, TelusCreditProfile telusCreditProfileToPatch) {
        String customerUid = customerEntity.getCreditProfileCustomerId();
        String primaryCustomerPartyIdDBId = customerEntity.getPartyId();

        TelusChannel aTelusChannel = telusCreditProfileToPatch.getChannel();
        validationService.validateForPatch(telusCreditProfileToPatch);

        Optional<CreditProfileEntity> custCpRelFromDB = Optional.empty();
        //request creditProfile has id=> get the existing customer creditprofile relation
        if (!StringUtils.isBlank(telusCreditProfileToPatch.getId())) {
            custCpRelFromDB = creditProfileDao.getByCustomerUidAndProfileId(customerUid, telusCreditProfileToPatch.getId());
            if (!custCpRelFromDB.isPresent()) {
                throw new CreditException(HttpStatus.BAD_REQUEST, ErrorCode.C_1120.code(), ErrorCode.C_1120.getMessage(),
                        "Profile " + telusCreditProfileToPatch.getId() +
                                // " LegacyProfileId " + legacyId +
                                " was not found or does not belong to customer " + customerUid);
            }
        } else {
           
        	
            LOGGER.info("finding CreditProfileEntity  by CustomerUid {}", customerUid);
            //get latest existing customer creditprofile relation
            //custCpRelFromDB = creditProfileDao.getLatestByCustomerUid(customerUid);

            //get latest CreditProfileEntity matching requestLineOfBusiness            
            List<CreditProfileEntity> dbCreditProfileEntityLst=creditProfileDao.getByCustomerUid(customerUid);
            if(dbCreditProfileEntityLst!=null) {
	           //get latest CreditProfileEntity matching requestLineOfBusiness
	            final String requestLineOfBusiness = (telusCreditProfileToPatch.getLineOfBusiness()!=null)?telusCreditProfileToPatch.getLineOfBusiness():"";
	            List<CreditProfileEntity> filteredSortedCreditProfileEntityList = dbCreditProfileEntityLst.stream()
	            .filter(entity -> entity.getLineOfBusiness().equals(requestLineOfBusiness))
	            .sorted(Comparator.comparing(CreditProfileEntity::getCreatedTs).reversed()) // sort by createdTs in descending order
	            .collect(Collectors.toList());
	            if(!filteredSortedCreditProfileEntityList.isEmpty() && filteredSortedCreditProfileEntityList.size()>0) {
		            CreditProfileEntity dbCreditProfileEntity=filteredSortedCreditProfileEntityList.get(0);
		            custCpRelFromDB=Optional.of(dbCreditProfileEntity);
	            }
            }

        }

    	if (custCpRelFromDB.isPresent()) {
            telusCreditProfileToPatch.setId(custCpRelFromDB.get().getCreditProfileId());
            LOGGER.info("found credit profile {} for customer {}", telusCreditProfileToPatch.getId(), customerUid);
        }
        //no customer creditprofile relation exist=> create new one
        else {
            this.createCreditProfile(customerUid, telusCreditProfileToPatch);
            return;
        }

        final String profileId = telusCreditProfileToPatch.getId();
        LOGGER.info("Credit profile exists {}", profileId);
        if (!StringUtils.isBlank(telusCreditProfileToPatch.getPrimaryCreditScoreCd())) {
            LOGGER.info("Credit profile {} has a creditScore value.", profileId);
        }
        //update creditprofie

        //fix for missing or empty CreditProfileDate in request
        if (telusCreditProfileToPatch.getCreatedTs() == null || telusCreditProfileToPatch.getCreatedTs().isEmpty()) {
            LOGGER.info("Request CreditProfileDate=<{}>", telusCreditProfileToPatch.getCreatedTs());
            //use dbCreditProfileTs
            Timestamp dbCreditProfileTs = (custCpRelFromDB != null && custCpRelFromDB.get() != null) ? custCpRelFromDB.get().getCreditProfileTs() : null;
            String dbCreditProfileTsStr = DateTimeUtils.toUtcString(dbCreditProfileTs);
            LOGGER.info("dbCreditProfileTsStr=<{}>", dbCreditProfileTsStr);
            if (dbCreditProfileTsStr != null && !dbCreditProfileTsStr.isEmpty()) {
                telusCreditProfileToPatch.setCreatedTs(dbCreditProfileTsStr);
            } else {
                //use requestCreditClassDate
                String requestCreditClassDate = (telusCreditProfileToPatch.getTelusCharacteristic() != null) ? telusCreditProfileToPatch.getTelusCharacteristic().getCreditClassDate() : null;
                LOGGER.info("requestCreditClassDate=<{}>", requestCreditClassDate);
                if (requestCreditClassDate != null && !requestCreditClassDate.isEmpty()) {
                    telusCreditProfileToPatch.setCreatedTs(requestCreditClassDate);
                } else {
                    //use current Date
                    telusCreditProfileToPatch.setCreatedTs(DateTimeUtils.toUtcString(new Date()));
                }
            }
        }

        int update = creditProfileDao.update(
                profileId,
                TelusCreditProfileModelMapper.toCreditProfileEntity(telusCreditProfileToPatch)
                        .version(custCpRelFromDB.get().getVersion(), true)
                        .updatedBy(aTelusChannel.getUserId())
                        .originatorAppId(aTelusChannel.getOriginatorAppId())
                        .channelOrgId(aTelusChannel.getChannelOrgId())
        );

        LOGGER.info("creditProfileDao.update operation for creditprofileId= {} returned updateCount={}", profileId, update);
        if (update > 1) {
            //Allow transaction to complete. if there are more than on entries in DB with the same ID , all shall be updated.
            String msg = "";
            msg = "[";
            msg = msg + "ActualSize=" + update;
            msg = msg + ",ExpectedSize=" + 1;
            msg = msg + ",Message=creditProfileDao.update operation updated multiple rows for same creditProfileId=" + profileId;
            msg = msg + "]";
            LOGGER.warn("{} Data Access Exception. {} . for creditprofileId= {} ", ExceptionConstants.POSTGRES100, profileId, msg);
        } else {
            if (update != 1) {
                throw new IncorrectResultSizeDataAccessException("creditProfileId=" + profileId + " creditProfileDao.update", 1, update);
            }
        }

        saveCreditWarnings(profileId, telusCreditProfileToPatch.getWarningHistoryList(), aTelusChannel,telusCreditProfileToPatch.getLineOfBusiness());
        updateProdCateQual(profileId, telusCreditProfileToPatch.getProductCategoryQualification(), aTelusChannel,telusCreditProfileToPatch.getLineOfBusiness());

        //STEP:update party data
        RelatedParty ownerCustRelatedParty = telusCreditProfileToPatch.getCustomerRelatedParty();
        if (ownerCustRelatedParty != null && ownerCustRelatedParty.getEngagedParty() != null) {
            engagedPartyService.patchEngagedParty(primaryCustomerPartyIdDBId, ownerCustRelatedParty, telusCreditProfileToPatch.getChannel(), telusCreditProfileToPatch.getCreditProfileConsentCd(), telusCreditProfileToPatch.getLineOfBusiness());
            if ("WIRELINE".equals(telusCreditProfileToPatch.getLineOfBusiness())) {

                if (Objects.nonNull(telusCreditProfileToPatch.getCreditProfileConsentCdPatch()) && "Y".equals(telusCreditProfileToPatch.getCreditProfileConsentCdPatch().get())) {
                   // performOverrideCreditWorthiness(Long.parseLong(relatedPartyToPatch.getId()), telusCreditProfileToPatch, telusCreditProfileToPatch.getChannel().getUserId(), telusCreditProfileToPatch.getChannel().getOriginatorAppId(), AUDIT_CREDIT_ASSESSMENT_TYPE);
                }
                if (Objects.nonNull(telusCreditProfileToPatch.getCreditClassCdPatch()) && "Y".equals(telusCreditProfileToPatch.getCreditClassCdPatch().get())) {
                   // performOverrideCreditWorthiness(Long.parseLong(relatedPartyToPatch.getId()), telusCreditProfileToPatch, telusCreditProfileToPatch.getChannel().getUserId(), telusCreditProfileToPatch.getChannel().getOriginatorAppId(), OVERRIDE_CREDIT_ASSESSMENT_TYPE);
                }
            }
        }
    }



/*    private void performOverrideCreditWorthiness(long customerId, CreditProfile modifiedCreditProfile, String userId,
                                                String applicationId, String creditAssessmentType) {

        OverrideCreditWorthinessRequest overrideCreditWorthinessRequest = new OverrideCreditWorthinessRequest();

        try {
            overrideCreditWorthinessRequest.setCreditAssessmentTypeCd(creditAssessmentType);
            if (creditAssessmentType.equals(creditWorthinessConfig.getOverrideCreditAssessmentType())) {
                overrideCreditWorthinessRequest.setNewCreditValueCd(modifiedCreditProfile.getCreditClassCd());
                if (!CollectionUtils.isEmpty(modifiedCreditProfile.getWarningHistoryList())) {
                    overrideCreditWorthinessRequest.setNewFraudIndictorCd(modifiedCreditProfile.getWarningHistoryList().get(0).getWarningStatusCd());
                }
                overrideCreditWorthinessRequest.setCreditAssessmentSubTypeCd(creditWorthinessConfig.getManualOverrideSubType());

            } else if (creditAssessmentType.equals(creditWorthinessConfig.getAuditCreditAssessmentType())) {

                overrideCreditWorthinessRequest.setCreditAssessmentSubTypeCd(creditWorthinessConfig.getBureauConsentSubType());
                overrideCreditWorthinessRequest.setNewCreditCheckConsentCd(modifiedCreditProfile.getCreditProfileConsentCd());
            }
            overrideCreditWorthinessRequest.setApplicationID(applicationId);
            overrideCreditWorthinessRequest.setLineOfBusiness("WIRELINE");
            overrideCreditWorthinessRequest.setCustomerID(customerId);
            if (!CollectionUtils.isEmpty(modifiedCreditProfile.getCharacteristics())) {
                Optional<String> commentTxtOptional = modifiedCreditProfile.getCharacteristics()
                        .stream()
                        .filter(characteristic -> characteristic.getName().equals(""))
                        .map(TelusCharacteristic::getValue)
                        .findFirst();
                commentTxtOptional.ifPresent(overrideCreditWorthinessRequest::setCommentTxt);
            }
        } catch (Exception e) {
            LOGGER.error("Error", e);
        }

        AuditInfo auditInfo = new AuditInfo();
        auditInfo.setUserId(userId);
        auditInfo.setOriginatorApplicationId(applicationId);

        try {
            servicePortType.overrideCreditWorthiness(overrideCreditWorthinessRequest, auditInfo);
        } catch (Exception e) {
            LOGGER.error("Error", e);
        }


    }*/

  
    
    /**
     * Create credit warning if it doesn't exists, update if it does
     *
     * @param creditProfileId uid string
     * @param warnings
     * @param auditCharacteristic
     */
    private void saveCreditWarnings(String creditProfileId, List<TelusCreditDecisionWarning> warnings, TelusChannel auditCharacteristic,String lineOfBusiness) {
    	
    	if ("WIRELINE".equalsIgnoreCase(lineOfBusiness) && auditCharacteristic.getTenpubsubsync()) {
    		savePubSubWirelineWarning(creditProfileId, warnings, auditCharacteristic);
    		return;
    	}
        for (TelusCreditDecisionWarning warning : CommonHelper.nullSafe(warnings)) {
            CreditWarningHistoryEntity entity = CreditWarningHistoryModelMapper.toEntity(warning);
            //new warning and not from telus pubsub 
            if (StringUtils.isBlank(warning.getId()) && !auditCharacteristic.getTenpubsubsync()) {
                validationService.validateForCreate(warning);
                LOGGER.info("Creating warning history for credit profile {}", creditProfileId);
                String id = creditWarningHistoryDao.insert(entity
                        .creditProfileId(creditProfileId)
                        .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                        .originatorAppId(auditCharacteristic.getOriginatorAppId())
                        .channelOrgId(auditCharacteristic.getChannelOrgId()));
                warning.setId(id);
            }
            else 
            {
	           //existing warning and not from telus pubsub
	            if (!auditCharacteristic.getTenpubsubsync()) {
	                Optional<CreditWarningHistoryEntity> existingWarning = creditWarningHistoryDao.getById(warning.getId());
	                if (!existingWarning.isPresent() || !existingWarning.get().getCreditProfileId().equals(creditProfileId)) {
	                    throw new CreditException(HttpStatus.BAD_REQUEST, ErrorCode.C_1122.code(), ErrorCode.C_1122.getMessage(),
	                            "Warning " + warning.getId() + " was not found or does not belong to profile "+ creditProfileId);
	                }
	                LOGGER.info("Updating warning history {}, credit profile {}", warning.getId(), creditProfileId);
	                
	                int updateCount = creditWarningHistoryDao.update(warning.getId(), entity
	                        .version(existingWarning.get().getVersion())
	                        .updatedBy(auditCharacteristic.getUserId())
	                        .originatorAppId(auditCharacteristic.getOriginatorAppId())
	                        .channelOrgId(auditCharacteristic.getChannelOrgId()));
	                if (updateCount > 1) {
	                    //Allow transaction to complete. if there are more than one entries in DB with the same ID , all shall be updated.
	                    	String msg="";
	                	    	msg= "[" ;
	                	    	msg = msg  + "ActualSize=" + updateCount;
	                	    	msg = msg  + ",ExpectedSize=" +1;
	                	    	msg = msg  + ",Message=creditWarningHistoryDao.update operation updated multiple rows for same creditProfileId=" +creditProfileId ;
	                	    	msg = msg  + "]";
	                        LOGGER.warn("{} Data Access Exception. {} ", ExceptionConstants.POSTGRES100,msg);                      
	                } else if (updateCount != 1) {
	                   throw new IncorrectResultSizeDataAccessException("creditProfileId=" +creditProfileId + " creditProfileDao.update",1,updateCount);
	                }
	                
	            } else {
	            	//warning from telus pubsub
	                savePubSubWarning(creditProfileId, warning, auditCharacteristic);
	            }
            }
        }
    }

    /**
     * Save/Create warning when source is from ten pubsub.
     *
     * @param creditProfileId uid string
     * @param warning
     * @param auditCharacteristic
     */
    private void savePubSubWarning(String creditProfileId, TelusCreditDecisionWarning warning, TelusChannel auditCharacteristic) {
        CreditWarningHistoryEntity entity = CreditWarningHistoryModelMapper.toEntity(warning);
        Optional<CreditWarningHistoryEntity> warn = Optional.empty();
        Long a = warning.getWarningHistoryLegacyId();
        if(warning.getWarningHistoryLegacyId() != null && warning.getWarningHistoryLegacyId() > 0) {
            warn = creditWarningHistoryDao.getByProfileIdAndLegacyId(creditProfileId, warning.getWarningHistoryLegacyId());
        }

        if (!warn.isPresent()) {
            LOGGER.info("Create warning for pubsub profileId={} legacyIdNew={}", creditProfileId, warning.getWarningHistoryLegacyIdNew());

            if (warning.getWarningHistoryLegacyIdNew() == null || warning.getWarningHistoryLegacyIdNew() == 0) {
            	LOGGER.warn("Cannot update existing warn due to LegacyIdNew is null.  profileId={} legacyIdOld={}", creditProfileId, warning.getWarningHistoryLegacyId() );
                return;
            }

            String id = creditWarningHistoryDao.insert(entity
                    .creditProfileId(creditProfileId)
                    .warningLegacyId(warning.getWarningHistoryLegacyIdNew())
                    .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                    .originatorAppId(auditCharacteristic.getOriginatorAppId())
                    .channelOrgId(auditCharacteristic.getChannelOrgId()));
            warning.setId(id);
        } else {
            LOGGER.info("update warning for pubsub profileId={} legacyId={} newLegacyId={}",
                    creditProfileId, warning.getWarningHistoryLegacyId(), warning.getWarningHistoryLegacyIdNew());

            int updateCount = creditWarningHistoryDao.update(warn.get().getWarningId(), entity
                    .warningLegacyId(warning.getWarningHistoryLegacyIdNew(), warning.getWarningHistoryLegacyIdNew() != null && warning.getWarningHistoryLegacyIdNew() != 0)
                    .version(warn.get().getVersion())
                    .updatedBy(auditCharacteristic.getUserId())
                    .originatorAppId(auditCharacteristic.getOriginatorAppId())
                    .channelOrgId(auditCharacteristic.getChannelOrgId())
                    );
            if (updateCount != 1) {
                //throw new IncorrectResultSizeDataAccessException("creditProfileId=" +creditProfileId + " creditWarningHistoryDao.update",1,updateCount);
                //Allow transaction to complete. if there are more than on entries in DB with the same ID , all shall be updated.
            	String msg="";
        	    	msg= "[" ;
        	    	msg = msg  + "ActualSize=" + updateCount;
        	    	msg = msg  + ",ExpectedSize=" +1;
        	    	msg = msg  + ",Message=creditWarningHistoryDao.update operation updated multiple rows for same creditProfileId=" +creditProfileId ;
        	    	msg = msg  + "]";
                LOGGER.warn("{} Data Access Exception. {} ", ExceptionConstants.POSTGRES100,msg);                   
            }
        }
    }

    private void savePubSubWirelineWarning(String creditProfileId, List<TelusCreditDecisionWarning> warnings, TelusChannel auditCharacteristic) {
    if (warnings ==null  || warnings.isEmpty()) {
    	return;
    }
    List<CreditWarningHistoryEntity> dbwarns = creditWarningHistoryDao.getWarningListByProfileIds(creditProfileId);
    if (dbwarns!=null && !dbwarns.isEmpty()) {
    	creditWarningHistoryDao.deleteByProfileId(creditProfileId);
    }
    
    for (TelusCreditDecisionWarning warning : CommonHelper.nullSafe(warnings)) {
        CreditWarningHistoryEntity entity = CreditWarningHistoryModelMapper.toEntity(warning);
        LOGGER.info("Create warning for pubsub profileId={} ", creditProfileId);
        String id = creditWarningHistoryDao.insert(entity
                .creditProfileId(creditProfileId)
                .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                .originatorAppId(auditCharacteristic.getOriginatorAppId())
                .channelOrgId(auditCharacteristic.getChannelOrgId()));
        warning.setId(id);        
    }

    }
    @Override    
    public void updateProdCateQual(String creditProfileId, List<ProductCategoryQualification> prodCatList,TelusChannel auditCharacteristic, String lineOfBusiness) {
        if (prodCatList ==null  || prodCatList.isEmpty()) {
        	return;
        }
    	if (!"WIRELINE".equalsIgnoreCase(lineOfBusiness) ) {
    		return;
    	}
    	
        Optional<List<ProdQualEntity>> dbwarns = prodQualDao.getByProfileUuid(creditProfileId);
        
        if (dbwarns!=null && dbwarns.get() !=null && !dbwarns.get().isEmpty() 
        		//&& auditCharacteristic.getTenpubsubsync()
        		) {
        	prodQualDao.deleteByProfileId(creditProfileId);
        }    	
        for (ProductCategoryQualification prodCat : CommonHelper.nullSafe(prodCatList)) {
 			ProdQualEntity prodCaEntity = ProdQualModelMapper.toEntity(creditProfileId, prodCat, auditCharacteristic);
            LOGGER.info("Create ProductCategoryQualification for pubsub profileId={} ", creditProfileId);
            String id = prodQualDao.insert(prodCaEntity
                    .creditProfileId(creditProfileId)
                    .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                    .originatorAppId(auditCharacteristic.getOriginatorAppId())
                    );
            prodCat.setProductQualId(id);      
        }		
	} 
     
    /**
     * Get all credit profiles for a customer
     *
     * @param creditProfileCustomerId string
     * @return
     */

	/* 
	 * get list of credit profiles (with customer-creditprofile relationship) and warnings by creditProfileCustomerId 
	 * */
    @Override
    public List<TelusCreditProfile> getCreditProfiles_Warnings_And_CustomerRelation_ByCreditProfileCustomerId(String creditProfileCustomerId) {
        List<TelusCreditProfile> telusCreditProfiles = new ArrayList<TelusCreditProfile>();

        Optional<List<CompositeEntity>> optionalCompositeList = customerCreditProfileRelDao.get_CreditProfile_CustomerCreditProfileRel_ByCreditProfileCustomerId(creditProfileCustomerId);
        Map<String, TelusCreditProfile> cpList = new HashMap<>();
        if (optionalCompositeList.isPresent()) {
            List<CompositeEntity> compositeList = optionalCompositeList.get();
            for (CompositeEntity aCompositeEntity : compositeList) {
                CreditProfileEntity aCreditProfilerEntity = aCompositeEntity.getEntity(0, CreditProfileEntity.class);
                TelusCreditProfile cp = TelusCreditProfileModelMapper.toCreditProfileDto(aCreditProfilerEntity);
                cp.setWarningHistoryList(new ArrayList<>());
                cpList.put(cp.getId(), cp);

                CustomerCreditProfileRelEntity aCustomerCreditProfileRelEntity = aCompositeEntity.getEntity(1, CustomerCreditProfileRelEntity.class);
                String customerCreditProfileRelCd = aCustomerCreditProfileRelEntity.getCustomerCreditProfileRelCd();
                cp.setCustomerCreditProfileRelCd(customerCreditProfileRelCd);
                telusCreditProfiles.add(cp);
            }
        }


        List<String> profileIds = telusCreditProfiles.stream().map(TelusCreditProfile::getId).collect(Collectors.toList());
        List<CreditWarningHistoryEntity> warningHistoryEntities = creditWarningHistoryDao.getByProfileIds(profileIds);
        warningHistoryEntities.forEach(we -> {
            TelusCreditDecisionWarning warning = CreditWarningHistoryModelMapper.toDto(we);
            cpList.get(we.getCreditProfileId()).getWarningHistoryList().add(warning);
        });

        //Prod Qual
        try {
            List<ProdQualEntity> prodQualEntities = prodQualDao.getByProfileIds(profileIds);
            prodQualEntities.forEach(we -> {
                ProductCategoryQualification productCategoryQualification = ProdQualModelMapper.toDto(we);
                List<ProductCategoryQualification> qualifications = cpList.get(we.getCreditProfileId()).getProductCategoryQualification();
                if (CollectionUtils.isEmpty(qualifications)) {
                    cpList.get(we.getCreditProfileId()).setProductCategoryQualification(new ArrayList<>());
                }
                cpList.get(we.getCreditProfileId()).getProductCategoryQualification().add(productCategoryQualification);

            });
        } catch (Exception e) {
            LOGGER.error("{} , Prod Qual Error {}",ExceptionConstants.STACKDRIVER_METRIC, creditProfileCustomerId, e);
        }

        return telusCreditProfiles;
    }
			
    @Override
    public String getCustomerUid(String creditProfileId) {
        Optional<CustomerCreditProfileRelEntity> customerCreditProfileRelOptional = customerCreditProfileRelDao.getCustomerCreditProfileRel(creditProfileId);
        String custUid = null;

        if (customerCreditProfileRelOptional.isPresent()) {
            custUid = customerCreditProfileRelOptional.get().getCreditProfileCustomerId();
        }

        return custUid;
    }
}
