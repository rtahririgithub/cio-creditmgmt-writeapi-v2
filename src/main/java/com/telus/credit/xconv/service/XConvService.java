package com.telus.credit.xconv.service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.dao.CreditProfileDao;
import com.telus.credit.dao.CreditWarningHistoryDao;
import com.telus.credit.dao.CustomerCreditProfileRelDao;
import com.telus.credit.dao.CustomerDao;
import com.telus.credit.dao.IdentificationAttributesDao;
import com.telus.credit.dao.IndividualDao;
import com.telus.credit.dao.OrganizationDao;
import com.telus.credit.dao.PartyContactMediumDao;
import com.telus.credit.dao.PartyDao;
import com.telus.credit.dao.ProdQualDao;
import com.telus.credit.dao.entity.CreditProfileEntity;
import com.telus.credit.dao.entity.CreditWarningHistoryEntity;
import com.telus.credit.dao.entity.CustomerCreditProfileRelEntity;
import com.telus.credit.dao.entity.CustomerEntity;
import com.telus.credit.dao.entity.IndividualEntity;
import com.telus.credit.dao.entity.OrganizationEntity;
import com.telus.credit.dao.entity.PartyContactMediumEntity;
import com.telus.credit.dao.entity.PartyEntity;
import com.telus.credit.dao.entity.PartyIdentificationExEntity;
import com.telus.credit.dao.entity.ProdQualEntity;
import com.telus.credit.dao.entity.XCreditProfileEntity;
import com.telus.credit.dao.entity.XProdqualEntity;
import com.telus.credit.dao.entity.XSyncStatusEntity;
import com.telus.credit.dao.entity.XWarningEntity;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.exceptions.PubSubPublishException;
import com.telus.credit.model.AccountInfo;
import com.telus.credit.model.Customer;
import com.telus.credit.model.ProductCategoryQualification;
import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.TelusAuditCharacteristic;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCreditDecisionWarning;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.model.mapper.CreditWarningHistoryModelMapper;
import com.telus.credit.model.mapper.TelusCreditProfileModelMapper;
import com.telus.credit.service.CustomerService;
import com.telus.credit.service.impl.CreditProfileChangedHandlerService;
import com.telus.credit.service.impl.CustomerCollectionService;
import com.telus.credit.service.impl.PartyIdentificationService;
import com.telus.credit.xconv.dao.XCreditProfileDao;
import com.telus.credit.xconv.dao.XProdqualDao;
import com.telus.credit.xconv.dao.XSyncStatusDao;
import com.telus.credit.xconv.dao.XWarningDao;
import com.telus.credit.xconv.mapper.XAccountInfoMapper;
import com.telus.credit.xconv.mapper.XEngagedPartyMapper;
import com.telus.credit.xconv.mapper.XPartyIdentificationMapper;
import com.telus.credit.xconv.mapper.XCreditProfileMapper;
import com.telus.credit.xconv.mapper.XProdqualMapper;
import com.telus.credit.xconv.mapper.XWarningMapper;

@Service
public class XConvService {

    private static final Logger LOGGER = LoggerFactory.getLogger(XConvService.class);

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private XCreditProfileDao xCreditProfileDao;

    @Autowired
    private XWarningDao xWarningDao;

    @Autowired
    private XProdqualDao xProdqualDao;
    
    @Autowired
    private CreditProfileDao creditProfileDao;

    @Autowired
    private CustomerCreditProfileRelDao creditProfileRelDao;

    @Autowired
    private CreditWarningHistoryDao warningHistoryDao;
    
    @Autowired
    private ProdQualDao prodQualDao;    

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PartyDao partyDao;

    @Autowired
    private IndividualDao individualDao;

    @Autowired
    private OrganizationDao organizationDao;

    @Autowired
    private IdentificationAttributesDao identificationAttributesDao;

    @Autowired
    private PartyIdentificationService identificationService;

    @Autowired
    private PartyContactMediumDao contactMediumDao;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private XSyncStatusDao xSyncStatusDao;

    @Autowired
    private CustomerCollectionService readDB;

    @Autowired
    private CreditProfileChangedHandlerService creditProfileChangedHandlerService;
    
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void syncCustomer(long custId,String conversionLineOfBusiness) {  
      	LOGGER.info("CustId={}. syncCustomer started", custId);
      	//get customerId that has need_to_sync = TRUE 
        Optional<XSyncStatusEntity> xsync = xSyncStatusDao.getByCustomerId(custId);
        if (!xsync.isPresent()) {
            LOGGER.warn("CustId={}.Sync status not found or need_to_sync is false in x_sync table", custId);
            return;
        }		 
		//get credprofile to be converted from converstion table x_credit_profile
        Optional<XCreditProfileEntity> xcpe = xCreditProfileDao.getXCreditProfileByCustomerID(custId);
        if (!xcpe.isPresent()) {
            LOGGER.warn("custId={} .. No x-credit-profile found ", custId);
            return ;
        }

        //search for existing customer by lineofbusiness in db
        Optional<CustomerEntity> customerEntity = customerDao.findCustomerEntityByIdForUpdate(custId,conversionLineOfBusiness);
        
        //found existing customer for the lineofbusiness
        if (customerEntity.isPresent()) {
        	 String dbLineOfBusiness = customerEntity.get().getLineOfBusiness();
        	 if(dbLineOfBusiness!=null && dbLineOfBusiness.equalsIgnoreCase(conversionLineOfBusiness)) {
	            try {
	            	conversionForExistingCustomer(custId, xcpe);
	            }catch(Exception e) {}
	            return ;    
        	 }
        }else {
        	conversionForNewCustomer(custId);
        }

        LOGGER.info("CustId={}. syncCustomer ended", custId);
    }

	private void conversionForExistingCustomer(long custId, Optional<XCreditProfileEntity> xcpe) throws Exception {
		LOGGER.info("CustId={}. conversionForExistingCustomer", custId);
		//map data from conversion X tables to telusCreditProfile (interface/swagger)
		TelusCreditProfile telusCreditProfile = XCreditProfileMapper.toTelusCreditProfile(xcpe.get());
                
        List<XWarningEntity> xWarningList = xWarningDao.getByCustomerId(custId);
        List<TelusCreditDecisionWarning> cpWarningList= new ArrayList<TelusCreditDecisionWarning> ();
        for (XWarningEntity xWarningEntity : xWarningList) {
        	TelusCreditDecisionWarning cpWarning = XWarningMapper.toCreditWarning(xWarningEntity);
        	cpWarningList.add(cpWarning);        	
		}
        telusCreditProfile.setWarningHistoryList(cpWarningList);
   
        List<XProdqualEntity> xProdqualList = xProdqualDao.getByCustomerId(custId);
        List<ProductCategoryQualification> productCategoryQualificationList = new ArrayList<ProductCategoryQualification>();
        for (XProdqualEntity xProdqualEntity : xProdqualList) {
        	ProductCategoryQualification p = XProdqualMapper.toProdQual(xProdqualEntity);
        	productCategoryQualificationList.add(p);
		}
        telusCreditProfile.setProductCategoryQualification(productCategoryQualificationList);

        
        long receivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
 		long submitterEventTime = System.currentTimeMillis();    
 		//call  API UPDATE  using telusCreditProfile (interface/swagger) 

 		try {
			objectMapper.writeValueAsString(telusCreditProfile);
		} catch (JsonProcessingException e) {}
	    creditProfileChangedHandlerService.processCreditProfileEventAsCustomer(
	    		 	telusCreditProfile, 
	    		 	null,//context, 
	    		 	receivedTime, 
	    		 	submitterEventTime, 
	    		 	"Data Conversion Delta", 
	    		 	"" //eventType
	    		 	);		
		xSyncStatusDao.update(custId, new XSyncStatusEntity().needToSync(false));
		LOGGER.info("CustId={}. xSyncStatusDao.update needToSync=false", custId);
	}
/*   
public void syncCustomerOld(long custId,String conversionLineOfBusiness) {  
      	LOGGER.info("CustId={}. syncCustomer started", custId);
      	//get customerId that has need_to_sync = TRUE 
        Optional<XSyncStatusEntity> xsync = xSyncStatusDao.getByCustomerId(custId);
        if (!xsync.isPresent()) {
            LOGGER.warn("CustId={}.Sync status not found or need_to_sync is false in x_sync table", custId);
            return;
        }
        
        
        Optional<CustomerEntity> customerEntity = customerDao.findCustomerEntityByIdForUpdate(custId,conversionLineOfBusiness);
        if (customerEntity.isPresent()) {
        	 String dbLineOfBusiness = customerEntity.get().getLineOfBusiness();
        	 if(dbLineOfBusiness!=null && dbLineOfBusiness.equalsIgnoreCase(conversionLineOfBusiness)) {
	            LOGGER.warn("custId={} .Customer already exist.", custId);
	            try {
	            	xSyncStatusDao.update(custId, new XSyncStatusEntity().needToSync(false));
	            }catch(Exception e) {}
	            return ;    
        	 }
        }     
        
        conversionForNewCustomer(custId);
        
        LOGGER.info("CustId={}. syncCustomer ended", custId);
    }
*/
private void conversionForNewCustomer(long custId) {
	LOGGER.info("CustId={}. conversionForNewCustomer", custId);
	TransactionTemplate txTemplate = new TransactionTemplate(txManager);
	txTemplate.executeWithoutResult(status -> {
		
	    try {
	    	//store customer in db
	        Map.Entry<String, XCreditProfileEntity> syncedCreditProfileEntity = syncCreditProfileEntities(custId);
	        if (syncedCreditProfileEntity == null) {
	            return;
	        }
	        xSyncStatusDao.update(custId, new XSyncStatusEntity().needToSync(false));
	        LOGGER.info("CustId={}. xSyncStatusDao.update needToSync=false", custId);
	        
	        //get customer from db and publish a msg to firestore
	        Customer aCustomerAndCreditProfiles = customerService.getCustomerPartyFromDatabase(custId);
	        if(aCustomerAndCreditProfiles!= null ) {
	            AccountInfo accountInfo = XAccountInfoMapper.fromEntity(syncedCreditProfileEntity.getValue());            
	            readDB.updateCustomerCollection(aCustomerAndCreditProfiles, accountInfo, "U", System.currentTimeMillis(), System.currentTimeMillis(),"");
	        }else {
	        	throw new Exception("getCustomerPartyFromDatabase returned null. CustId= " + custId );
	        }
	    
	    } catch (Exception e) {
	        if (e instanceof PubSubPublishException) {
	            LOGGER.error("CustId={}.{} Error publishing sync message. {} ", custId,ExceptionConstants.PUBSUB202, ExceptionHelper.getStackTrace(e));
	        } else if (e instanceof DataAccessException || e instanceof CannotCreateTransactionException) {
	            LOGGER.error("CustId={}.{} Database error. {} ", custId,ExceptionConstants.POSTGRES101, ExceptionHelper.getStackTrace(e));
	        } else {
	            LOGGER.error("CustId={}.Error syncing {}.", custId, ExceptionHelper.getStackTrace(e));
	        }
	        status.setRollbackOnly();
	    }
      });
}

    /**
     * Sync credit profile, engaged party and warnings from temporary tables
     *syncEngagedParty
     *syncIdentification
     *syncContact
     *syncCusomer
     *syncCreditProfileEntity
     *syncWarning
     *syncProdqual
     * @param custId
     * @return (Credit profile uid, entity)
     * @throws Exception
     */
    
    public Map.Entry<String, XCreditProfileEntity> syncCreditProfileEntities(long custId) throws Exception {
    	LOGGER.info("CustId={}. syncCreditProfileEntities started", custId);
    	//get credprofile to be converted from converstion table x_credit_profile
        Optional<XCreditProfileEntity> xcpe = xCreditProfileDao.getXCreditProfileByCustomerID(custId);
        if (!xcpe.isPresent()) {
            LOGGER.warn("custId={} .. No x-credit-profile found ", custId);
            return null;
        }
        String x_credit_profile_table_LineOfBusiness =xcpe.get().getLineOfBusiness();
        String partyUid = syncPartyEntity(xcpe.get());
        syncIdentification(xcpe.get(), partyUid);
        syncPartyContactMediumEntity(xcpe.get(), partyUid);
        final String custUid;
        custUid = customerDao.insert(new CustomerEntity().customerId(custId).partyId(partyUid)
                    .createdBy(xcpe.get().getCreatedBy())
                    .updatedBy(xcpe.get().getUpdatedBy())
                    .originatorAppId(xcpe.get().getOriginatorAppId())
                    .channelOrgId(xcpe.get().getChannelOrgId())
                    .lineOfBusiness(x_credit_profile_table_LineOfBusiness)
                    .role("Customer"));
        String creditProfileUid = 
        syncCreditProfileEntity(xcpe.get(), custUid,x_credit_profile_table_LineOfBusiness);
        syncWarning(xWarningDao.getByCustomerId(custId), creditProfileUid);
        syncProdqual(xProdqualDao.getByCustomerId(custId), creditProfileUid);

        LOGGER.info("CustId={}. syncCreditProfileEntities ended", custId);
        return new AbstractMap.SimpleEntry<>(custUid, xcpe.get());
    }

    
    /**
     * Sync credit profile from temp table
     *
     * @param xcpe
     * @param custUid
     * @return credit profile uid generated when inserting to credit_profile table
     */
    private String syncCreditProfileEntity(XCreditProfileEntity xcpe, String custUid,String x_credit_profile_table_LineOfBusiness) {
        CreditProfileEntity cpe = XCreditProfileMapper.toCreditProfileEntity(xcpe);
        cpe.setLineOfBusiness(x_credit_profile_table_LineOfBusiness);
        String cpUid = creditProfileDao.insert(cpe);
        
        creditProfileRelDao.insert(
        		new CustomerCreditProfileRelEntity()
                .creditProfileId(cpUid)
                .creditProfileCustomerId(custUid)
                .createdBy(cpe.getCreatedBy())
                .createdOnTs(cpe.getCreatedTs())
                .updatedBy(cpe.getUpdatedBy())
                .updatedOnTs(cpe.getUpdatedTs())
                .originatorAppId(cpe.getOriginatorAppId())
                .channelOrgId(cpe.getChannelOrgId())
                .customerCreditProfileRelCd(xcpe.getCproflCustMapTypCd())
        		);

        return cpUid;
    }

    private void syncWarning(List<XWarningEntity> xWrnList, String creditProfileUid) {
        for (XWarningEntity xWarningEntity : xWrnList) {
            CreditWarningHistoryEntity creditWarningHistoryEntity = XWarningMapper.toCreditWarningEntity(xWarningEntity);
            warningHistoryDao.insert(creditWarningHistoryEntity.creditProfileId(creditProfileUid));
        }
    }

    private void syncProdqual(List<XProdqualEntity> xProdQualList, String creditProfileUid) {
        for (XProdqualEntity xProdqualEntity : xProdQualList) {
        	 ProdQualEntity prodQualEntity = XProdqualMapper.toProdQualEntity(xProdqualEntity);
        	 prodQualDao.insert(prodQualEntity.creditProfileId(creditProfileUid));
        }
    }
    
    private String syncPartyEntity(XCreditProfileEntity xcpe) {
        PartyEntity partyEntity = XEngagedPartyMapper.mapPartyEntity(xcpe);
        return partyDao.insert(partyEntity);
    }

    private void syncPartyContactMediumEntity(XCreditProfileEntity xcpe, String partyUid) {
        PartyContactMediumEntity partyContactMediumEntity = XCreditProfileMapper.mapContactMedium(xcpe);
        contactMediumDao.insert(partyContactMediumEntity.partyId(partyUid));
    }

    //IndividualEntity , PartyIdentificationExEntity
    private void syncIdentification(XCreditProfileEntity xcpe, String partyUid) throws Exception {
        IndividualEntity individualEntity = XEngagedPartyMapper.mapIndividualEntity(xcpe);
        if (individualEntity != null) {
            individualDao.insert(individualEntity.partyId(partyUid));
        }

        OrganizationEntity organizationEntity = XEngagedPartyMapper.mapOrganizationEntity(xcpe);
        if (organizationEntity != null) {
            organizationDao.insert(organizationEntity.partyId(partyUid));
        }

        if (individualEntity == null && organizationEntity == null) {
           throw new IllegalStateException("Cannot distinguish Individual or Organization " + xcpe.getCustomerId() + " type "+ xcpe.getReferredtype());
//            LOGGER.error("Cannot distinguish Individual or Organization custid={} type {}. Use Individual as default", xcpe.getCustomerId(), xcpe.getReferredtype());
//            individualDao.insert(new IndividualEntity().partyId(partyUid));
        }

        List<String> encryptedAttrs = identificationAttributesDao.selectAll().stream()
                .filter(e -> e.getIsEncrypted()).map(e -> e.getAttributeName())
                .collect(Collectors.toList());

        List<PartyIdentificationExEntity> partyIdentificationExEntities = XPartyIdentificationMapper.toPartyIdentificationEntity(xcpe, partyUid, encryptedAttrs);
        TelusChannel auditCharacteristic = new TelusChannel();
        auditCharacteristic.setUserId(xcpe.getCreatedBy());
        auditCharacteristic.setOriginatorAppId(xcpe.getOriginatorAppId());
        auditCharacteristic.setChannelOrgId(xcpe.getChannelOrgId());        
        //partyIdentificationEntity,IdentificationCharEntity,IdentificationCharHashEntity
        for (PartyIdentificationExEntity partyIdentificationExEntity : partyIdentificationExEntities) {
            identificationService.createIdentification(partyUid, partyIdentificationExEntity, auditCharacteristic);
        }
    }

    private void removeCustomerCps(CustomerEntity customerEntity, String partyId) {
        List<CreditProfileEntity> creditProfiles = creditProfileDao.getByCustomerUid(customerEntity.getCreditProfileCustomerId());
        List<String> cpUids = creditProfiles.stream().map(e -> e.getCreditProfileId()).collect(Collectors.toList());
        
        LOGGER.debug("{} warnings were remove. Credit profile {}",
                warningHistoryDao.deleteByProfileIds(cpUids), cpUids);

        LOGGER.debug("{} Credit profile rels removed: {}",
                creditProfileRelDao.deleteByProfileIds(cpUids), cpUids);

        LOGGER.debug("{} Credit profiles removed: {}",
                creditProfileDao.deleteByProfileIds(cpUids), cpUids);

        List<PartyIdentificationExEntity> identificationEntities = identificationService.getIdentificationEntities(partyId);
        identificationEntities.forEach(e -> identificationService.removeAll(partyId));
        LOGGER.debug("Individual removed: {}, partyId={}",
                individualDao.removeByPartyIds(Collections.singletonList(partyId)), partyId);
        LOGGER.debug("Organization removed: {}, partyId={}",
                organizationDao.removeByPartyIds(Collections.singletonList(partyId)), partyId);
        LOGGER.debug("Contact removed: {}, partyId={}",
                contactMediumDao.removeByPartyIds(Collections.singletonList(partyId)), partyId);
        LOGGER.debug("Party removed {} uid={}",
                partyDao.removeById(partyId), partyId);
    }
}
