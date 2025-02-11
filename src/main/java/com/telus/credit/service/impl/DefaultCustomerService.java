package com.telus.credit.service.impl;

import com.telus.credit.common.CommonHelper;
import com.telus.credit.common.CreditMgmtCommonConstants;
import com.telus.credit.common.ErrorCode;
import com.telus.credit.common.RequestContext;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.dao.CreditProfileDao;
import com.telus.credit.dao.CreditWarningHistoryDao;
import com.telus.credit.dao.CustomerCreditProfileRelDao;
import com.telus.credit.dao.CustomerDao;
import com.telus.credit.dao.ProdQualDao;
import com.telus.credit.dao.Queries;
import com.telus.credit.dao.ReadDbSyncStatusDao;
import com.telus.credit.dao.entity.CreditProfileEntity;
import com.telus.credit.dao.entity.CreditWarningHistoryEntity;
import com.telus.credit.dao.entity.CustomerCreditProfileRelEntity;
import com.telus.credit.dao.entity.CustomerEntity;
import com.telus.credit.dao.entity.PartyEntity;
import com.telus.credit.dao.entity.ProdQualEntity;
import com.telus.credit.dao.entity.ReaddbSyncStatusEntity;
import com.telus.credit.dao.mapper.CompositeRowMapper;
import com.telus.credit.dao.mapper.CompositeRowMapper.CompositeEntity;
import com.telus.credit.dao.rowmapper.CustomerRowMapper;
import com.telus.credit.dao.rowmapper.PartyRowMapper;
import com.telus.credit.exceptions.CreditException;
import com.telus.credit.exceptions.CryptoException;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.exceptions.PubSubPublishException;
import com.telus.credit.model.AccountInfo;
import com.telus.credit.model.Attachments;
import com.telus.credit.model.CreditProfileAuditDocument;
import com.telus.credit.model.Customer;
import com.telus.credit.model.CustomerToPatch;
import com.telus.credit.model.Individual;
import com.telus.credit.model.Organization;
import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.RelatedPartyInterface;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCreditDecisionWarning;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.model.common.PartyType;
import com.telus.credit.model.mapper.CreditWarningHistoryModelMapper;
import com.telus.credit.model.mapper.ProdQualModelMapper;
import com.telus.credit.model.mapper.TelusCreditProfileModelMapper;
import com.telus.credit.pubsub.service.TelusCreditProfileEventSender;
import com.telus.credit.pubsub.service.TelusMDMEventSender;
import com.telus.credit.service.CreditProfileService;
import com.telus.credit.service.CustomerService;
import com.telus.credit.service.EngagedPartyService;
import com.telus.credit.service.ValidationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class DefaultCustomerService implements CustomerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCustomerService.class);

	private PlatformTransactionManager txManager;

	@Autowired
	private CustomerDao customerDao;

	@Autowired
	private ProdQualDao prodQualDao;

	@Autowired
	private CreditProfileDao creditProfileDao;
	@Autowired
	private CustomerCreditProfileRelDao customerCreditProfileRelDao;
	@Autowired
	private CreditWarningHistoryDao creditWarningHistoryDao;
	@Autowired
	private ReadDbSyncStatusDao readDbSyncStatusDao;

	private CreditProfileService<TelusCreditProfile> creditProfileService;

	@Autowired
	private EngagedPartyService engagedPartyService;
	
	private CustomerCollectionService readDB;
	
	@Autowired
	private TelusCreditProfileEventSender creditProfileEventSender;
	@Autowired
	private TelusMDMEventSender mdmEventSender;

	private ValidationService validationService;

	private ResponseInterceptorService responseInterceptorService;


	private static CryptoService cryptoService;

	@Autowired
	public void setEncryptionService(CryptoService cryptoService) {
		DefaultCustomerService.cryptoService = cryptoService;
	}

	public DefaultCustomerService(PlatformTransactionManager txManager, CustomerDao customerDao,
								  ReadDbSyncStatusDao readDbSyncStatusDao, CreditProfileService<TelusCreditProfile> creditProfileService,
								  EngagedPartyService engagedPartyService, CustomerCollectionService readDB,
								  ValidationService validationService, 
								  ResponseInterceptorService responseInterceptorService,
								  TelusCreditProfileEventSender creditProfileEventSender) {
		this.txManager = txManager;
		this.customerDao = customerDao;
		this.readDbSyncStatusDao = readDbSyncStatusDao;
		this.creditProfileService = creditProfileService;
		this.readDB = readDB;
		this.validationService = validationService;
		this.responseInterceptorService = responseInterceptorService;
		this.creditProfileEventSender=creditProfileEventSender;
	}	
	
	/**
	 * Create new customer or update if it exists.
	 * This method doesn't accept transaction because it contains transactions inside
	 *
	 * @param requestContext
	 * @param primaryCustomerId
	 * @param customerToPatch
	 * @return Full Instance of a customer
	 */
	@Transactional(propagation = Propagation.NEVER)
	//@Retryable (value = DataAccessException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
	//@Retryable (value = {CreditException.class, DataAccessException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
	@Override
	public Customer saveCustomerById(RequestContext requestContext, Long primaryCustomerId, CustomerToPatch customerToPatch, AccountInfo accountInfo, long receivedTime, long submitterEventTime,String eventDescription) {
 
		CreditProfileAuditDocument auditDocument = AuditService.auditContext();
		String corrId = UUID.randomUUID().toString();
		if(requestContext!=null) {
			corrId = requestContext.getCorrId();
		}
		
		auditDocument.setCorrelationId(corrId);
		auditDocument.setCustomerId(Objects.toString(primaryCustomerId));
		Optional.ofNullable(customerToPatch.getTelusAuditCharacteristic()).ifPresent(auditCharacteristic -> {
			auditDocument.setChannelOrgId(auditCharacteristic.getChannelOrganizationId());
			auditDocument.setOriginatingAppId(auditCharacteristic.getOriginatorApplicationId());
			auditDocument.setUserId(auditCharacteristic.getUserId());
		});
		auditDocument.setEventTimestamp(Timestamp.from(Instant.now()));
		String incommingLineofbusiness = 
				(
						   customerToPatch.getCreditProfile()!=null 
						&& !customerToPatch.getCreditProfile().isEmpty()
						&& customerToPatch.getCreditProfile().get(0) !=null 
						&& customerToPatch.getCreditProfile().get(0).getLineOfBusiness()!=null
				)
				? customerToPatch.getCreditProfile().get(0).getLineOfBusiness():"UNKNOWN";
				

		final Map.Entry<String, CustomerWithCreateUpdateFlag> customerInDB;
		
		TransactionTemplate txTemplate = new TransactionTemplate(txManager);
		customerInDB = txTemplate.execute(status -> {
			
			//Optional<CustomerEntity> customerEntity = customerDao.findCustomerById(custId);
			//Optional<CustomerEntity> customerEntity = customerDao.findCustomerEntityByIdForUpdate(primaryCustomerId);
			Optional<CustomerEntity> customerEntity = customerDao.findCustomerEntityByIdForUpdate(primaryCustomerId,incommingLineofbusiness);
			
			final String dbRelatedPartyCustomerRoleCustUid;
			String createUpdateFlag = "U";
	  //createCustomer
			if (!customerEntity.isPresent() || customerEntity.get().getCustomerId() == null) {
				auditDocument.setEventType(CreditProfileAuditDocument.EventType.CP_CREATE);
				createUpdateFlag = "C"; 
				
				//STEP: persist CreditProfile Resource in db( party, customer , creditprofile, relationship
				dbRelatedPartyCustomerRoleCustUid = persistNewCreditProfileResource(primaryCustomerId, customerToPatch.getCreditProfile().get(0));
				
				LOGGER.info("Insert read db sync for customer {} with needToSync=true", primaryCustomerId);
				readDbSyncStatusDao.insert(new ReaddbSyncStatusEntity().creditProfileCustomerId(dbRelatedPartyCustomerRoleCustUid).needToSync(true));				
 
			} 
	  //patchCustomer
			else {
				auditDocument.setEventType(CreditProfileAuditDocument.EventType.CP_UPDATE);
				
				dbRelatedPartyCustomerRoleCustUid = patchCustomer(customerEntity.get(), customerToPatch);
				
				// data migration, some data don't exist
				if (readDbSyncStatusDao.update(dbRelatedPartyCustomerRoleCustUid, new ReaddbSyncStatusEntity().needToSync(true)) == 0) {
					readDbSyncStatusDao.insert(new ReaddbSyncStatusEntity().creditProfileCustomerId(dbRelatedPartyCustomerRoleCustUid).needToSync(true));
				}
			}
			 
		//get updated customer and creditprofile(s) from DB    
			 //STEP: get complete customer from db	
			Customer customerFromDatabase = getCustomerPartyFromDatabase(primaryCustomerId);
			if(customerFromDatabase!=null) {
				customerFromDatabase.setCustUid(dbRelatedPartyCustomerRoleCustUid);	
			}
			
			if(customerToPatch.getCreditProfile()!=null) {
				for (TelusCreditProfile telusCreditProfile : customerToPatch.getCreditProfile()) {
					responseInterceptorService.resolveMissingFieldsAndAudit( telusCreditProfile,customerFromDatabase);
				}
				//TelusCreditProfile creditProfileToPatch = customerToPatch.getCreditProfile().get(0);
				//responseInterceptorService.resolveMissingFieldsAndAudit( creditProfileToPatch,customerFromDatabase);
			}
			
			CustomerWithCreateUpdateFlag c = new CustomerWithCreateUpdateFlag(customerFromDatabase, createUpdateFlag);
			return new AbstractMap.SimpleEntry<>(dbRelatedPartyCustomerRoleCustUid, c);
		});

		//publish firestore sync message		
		List<Attachments> attachments = (customerToPatch.getCreditProfile()!=null && customerToPatch.getCreditProfile().size() >0)?customerToPatch.getCreditProfile().get(0).getAttachments():new ArrayList<Attachments>();		
		publishFirestoreSyncCreditProfileMessage(
				attachments, 
				accountInfo,
				receivedTime, submitterEventTime, eventDescription, customerInDB,incommingLineofbusiness);
		
		//STEP: prepare the Response : decrypt and Resolve fields which were not stored in database
		responseInterceptorService.decryptCustomerFromDb(customerInDB.getValue().getCustomerFromDatabase());
		
	    //STEP: publish the incomingCreditProfile to world
		String eventType;
		if(CreditProfileAuditDocument.EventType.CP_CREATE.equals(auditDocument.getEventType()) ){
			eventType = "CreditProfileCreateEvent" ;
		}else {
			eventType="CreditProfileAttributeValueChangeEvent";	
		}
		creditProfileEventSender.publish(customerToPatch.getCreditProfile(),eventType);
	
    	
		return customerInDB.getValue().getCustomerFromDatabase();
	}

	/*
	 * update credirpofile table 
	 * update warning table 
	 * get complete data ( customer, creditprofile , warning ) from db
	 * publish firestore event
	 * */
	@Override
	@Transactional
	public Customer updateCreditProfileResourceByCPId(
				RequestContext requestContext, 
				String profileUiid, 
				TelusCreditProfile creditProfileToPatch, 
				AccountInfo accountInfo, 
				long receivedTime, 
				long submitterEventTime, String eventDescription) {
		validationService.validateForPatch(creditProfileToPatch);

	//update Database
 TransactionTemplate txTemplate = new TransactionTemplate(txManager);			
 final Map.Entry<String, CustomerWithCreateUpdateFlag> customerInDB = txTemplate.execute(status -> {

		//get existing creditprofile from db.
		Optional<CreditProfileEntity> existingCreditProfileEntity = creditProfileDao.getByCreditProfileUid(profileUiid);		
		if (!existingCreditProfileEntity.isPresent()) {
			throw new CreditException(HttpStatus.NOT_FOUND, ErrorCode.C_1120.code(), ErrorCode.C_1120.getMessage(),
					"Profile " + profileUiid +
							" was not found or does not belong to a customer ");
		}	 
	 
	 	//get customer and party ID  by credirpofile ID 
	 	Optional<CustomerEntity> primaryCustomerEntity=customerCreditProfileRelDao.getPrimaryCustomerByCreditProfileId(profileUiid);	
	 	//get customer ID (external IDs such as BAN,customerID) 
	 	long primaryCustomerId =primaryCustomerEntity.get().getCustomerId();	 	
	 	//get customer db pk ID (uuid) 
	 	String primaryCustomerDBId = primaryCustomerEntity.get().getCreditProfileCustomerId();        
	 	String primaryCustomerPartyIdDBId = primaryCustomerEntity.get().getPartyId();
	 	
		//STEP:update credit_profile table		 
			updateCreditProfileEntity(profileUiid, creditProfileToPatch, existingCreditProfileEntity);		
		//STEP:update credit_waring_history table 
	        createOrUpdateCreditWarnings(profileUiid, creditProfileToPatch);

			//STEP: update Prod Qual
			//updateProdQual(profileUiid, creditProfileToPatch);
			creditProfileService.updateProdCateQual(profileUiid, creditProfileToPatch.getProductCategoryQualification(), creditProfileToPatch.getChannel(),creditProfileToPatch.getLineOfBusiness());


	    //STEP:update party data

	        RelatedParty ownerCustRelatedParty = creditProfileToPatch.getCustomerRelatedParty();	

	        if (ownerCustRelatedParty!=null && ownerCustRelatedParty.getEngagedParty() != null) {
				engagedPartyService.patchEngagedParty(primaryCustomerPartyIdDBId, ownerCustRelatedParty, creditProfileToPatch.getChannel(),creditProfileToPatch.getCreditProfileConsentCd(),creditProfileToPatch.getLineOfBusiness());
			}	        
	        
	    //STEP:get complete customer and creditprofile(s) from DB         	        
	      //STEP: get complete customer from db	      
	        Customer customerFromDatabase = getCustomerPartyFromDatabase(primaryCustomerId);	        
			if(customerFromDatabase!=null) {
				customerFromDatabase.setCustUid(primaryCustomerDBId);	
			}			
			//STEP:publish change audit doc to be stored in firestore
			CreditProfileAuditDocument auditDocument = AuditService.auditContext();
			//auditDocument.setCorrelationId(requestContext.getCorrId());
			auditDocument.setCustomerId(Objects.toString(primaryCustomerId));
			auditDocument.setChannelOrgId(creditProfileToPatch.getChannel().getChannelOrgId());
			auditDocument.setOriginatingAppId(creditProfileToPatch.getChannel().getOriginatorAppId());
			auditDocument.setUserId(creditProfileToPatch.getChannel().getUserId());
			auditDocument.setEventTimestamp(Timestamp.from(Instant.now()));
			auditDocument.setEventType(CreditProfileAuditDocument.EventType.CP_UPDATE);
			responseInterceptorService.resolveMissingFieldsAndAudit(creditProfileToPatch,customerFromDatabase);

			
			CustomerWithCreateUpdateFlag c = new CustomerWithCreateUpdateFlag(customerFromDatabase, "U");			
			return new AbstractMap.SimpleEntry<>(primaryCustomerDBId, c);
 });       
 
	//publish complete customer to read db(firestore) 			
		publishFirestoreSyncCreditProfileMessage(
					 creditProfileToPatch.getAttachments(), 
					 accountInfo,
					receivedTime, 
					submitterEventTime, 
					eventDescription, 
					customerInDB,creditProfileToPatch.getLineOfBusiness());

	
		// Resolve fields which were not stored in database
		responseInterceptorService.decryptCustomerFromDb(customerInDB.getValue().getCustomerFromDatabase());

	    //STEP: publish the incomingCreditProfile to world
		creditProfileEventSender.publish(Collections.singletonList(creditProfileToPatch),"CreditProfileAttributeValueChangeEvent");

		return customerInDB.getValue().getCustomerFromDatabase();
	}

/*	
	private void updateProdQual(String profileUiid, TelusCreditProfile creditProfileToPatch) {
		List<ProductCategoryQualification> prodQualEntitiesPatch = creditProfileToPatch.getProductCategoryQualification();
		List<ProdQualEntity> prodQualEntities = prodQualDao.getByProfileIds(Lists.newArrayList(profileUiid));
		TelusChannel telusChannel = creditProfileToPatch.getChannel();
		//There are no existing prod qual for the credit profile id in db
		if (CollectionUtils.isEmpty(prodQualEntities)) {
			if (!CollectionUtils.isEmpty(prodQualEntitiesPatch)) {
				prodQualEntities = ProdQualModelMapper.toEntity(profileUiid, prodQualEntitiesPatch, creditProfileToPatch.getChannel());
				prodQualEntities.forEach(prodQualEntity -> prodQualDao.insert(prodQualEntity));
			}
		} else {
			//There are existing prod qual for the credit profile id in db
			Map<String, ProdQualEntity> prodQualEntityMap = prodQualEntities.stream().collect(Collectors.toMap(ProdQualEntity::getProductQualId, prodQualEntity -> prodQualEntity));
			//There are prod quals in the credit profile patch
			if (!CollectionUtils.isEmpty(prodQualEntitiesPatch)) {
				prodQualEntitiesPatch.forEach(productCategoryQualification -> {
					//Prod qual found in db, update it.
					if (prodQualEntityMap.containsKey(productCategoryQualification.getProductQualId())) {
						String prodQualId = productCategoryQualification.getProductQualId();
						ProdQualEntity updateProdQualEntity = prodQualEntityMap.get(prodQualId);
						updateProdQualEntity.productQualInd(productCategoryQualification.getQualified())
								.creditApprvdProdCatgyCd(productCategoryQualification.getCategoryCd())
								.originatorAppId(telusChannel.getChannelOrgId())
								.updatedBy(telusChannel.getUserId());
						prodQualDao.update(prodQualId, updateProdQualEntity);
						prodQualEntityMap.remove(productCategoryQualification.getProductQualId());
					} else {
						//Prod qual not found in db, create new one.
						ProdQualEntity prodQualEntity = ProdQualModelMapper.toEntity(profileUiid, productCategoryQualification, creditProfileToPatch.getChannel());
						prodQualDao.insert(prodQualEntity);
					}
				});
			}
			//These are prod quals removed from credit profile
			prodQualEntityMap.values().forEach(
					prodQualEntity -> {
						prodQualEntity.updatedBy(telusChannel.getUserId());
						prodQualDao.remove(prodQualEntity.getProductQualId(), prodQualEntity);
					}
			);
		}

	}
*/
	private void updateCreditProfileEntity(
									String profileUiid
									,TelusCreditProfile creditProfileToPatch
									,Optional<CreditProfileEntity> existingCreditProfileEntity) {
		
		CreditProfileEntity creditProfileEntity = TelusCreditProfileModelMapper.toCreditProfileEntity(creditProfileToPatch)
	               .version(existingCreditProfileEntity.get().getVersion(), true)
	                .updatedBy(creditProfileToPatch.getChannel().getUserId())
	                .originatorAppId(creditProfileToPatch.getChannel().getOriginatorAppId())
	                .channelOrgId(creditProfileToPatch.getChannel().getChannelOrgId() );
		
		int update = creditProfileDao.update(profileUiid,creditProfileEntity);
		
		LOGGER.info("creditProfileDao.update operation for creditprofileId= {} returned updateCount={}", profileUiid,update);
		if (update > 1) {
		    //Allow transaction to complete. if there are more than on entries in DB with the same ID , all shall be updated.
		    String msg="";
		    msg= "[" ;
		    msg = msg  + "ActualSize=" + update;
		    msg = msg  + ",ExpectedSize=" +1;
		    msg = msg  + ",Message=creditProfileDao.update operation updated multiple rows for same creditProfileId=" +profileUiid ;
		    msg = msg  + "]";
		    LOGGER.warn("{} Data Access Exception. {} . for creditprofileId= {} ", ExceptionConstants.POSTGRES100,profileUiid,msg);
		}else {
		    if (update != 1) {
		        throw new IncorrectResultSizeDataAccessException("creditProfileId=" +profileUiid + " creditProfileDao.update",1,update);
		    }
		}
	}

    /**
     * Create credit warning if it doesn't exists, update if it does
     *
     * @param profileUiid uid string
     * @param creditProfile
     */
    private void createOrUpdateCreditWarnings(String profileUiid, TelusCreditProfile creditProfile) {
    	
    	List<TelusCreditDecisionWarning> warnings = creditProfile.getWarningHistoryList();
         TelusChannel aTelusChannel = creditProfile.getChannel();
    	
        for (TelusCreditDecisionWarning warning : CommonHelper.nullSafe(warnings)) {
            CreditWarningHistoryEntity entity = CreditWarningHistoryModelMapper.toEntity(warning);
            //new warning and not from telus pubsub 
            if (StringUtils.isBlank(warning.getId()) && !aTelusChannel.getTenpubsubsync()) {
                validationService.validateForCreate(warning);
                LOGGER.info("Creating warning history for credit profile {}", profileUiid);
                String id = creditWarningHistoryDao.insert(entity
                        .creditProfileId(profileUiid)
                        .createdBy(aTelusChannel.getUserId()).updatedBy(aTelusChannel.getUserId())
                        .originatorAppId(aTelusChannel.getOriginatorAppId())
                        .channelOrgId(aTelusChannel.getChannelOrgId()));
                warning.setId(id);
            }
            else            
           //existing warning and not from telus pubsub
            if (!aTelusChannel.getTenpubsubsync()) {
                Optional<CreditWarningHistoryEntity> existingWarning = creditWarningHistoryDao.getById(warning.getId());
                if (!existingWarning.isPresent() || !existingWarning.get().getCreditProfileId().equals(profileUiid)) {
                    throw new CreditException(HttpStatus.BAD_REQUEST, ErrorCode.C_1122.code(), ErrorCode.C_1122.getMessage(),
                            "Warning " + warning.getId() + " was not found or does not belong to profile "+ profileUiid);
                }
                LOGGER.info("Updating warning history {}, credit profile {}", warning.getId(), profileUiid);
                
                int updateCount = creditWarningHistoryDao.update(warning.getId(), entity
                        .version(existingWarning.get().getVersion())
                        .updatedBy(aTelusChannel.getUserId())
                        .originatorAppId(aTelusChannel.getOriginatorAppId())
                        .channelOrgId(aTelusChannel.getChannelOrgId()));
                if (updateCount > 1) {
                    //Allow transaction to complete. if there are more than one entries in DB with the same ID , all shall be updated.
                    	String msg="";
                	    	msg= "[" ;
                	    	msg = msg  + "ActualSize=" + updateCount;
                	    	msg = msg  + ",ExpectedSize=" +1;
                	    	msg = msg  + ",Message=creditWarningHistoryDao.update operation updated multiple rows for same creditProfileId=" +profileUiid ;
                	    	msg = msg  + "]";
                        LOGGER.warn("{} Data Access Exception. {} ", ExceptionConstants.POSTGRES100,msg);                      
                } else if (updateCount != 1) {
                   throw new IncorrectResultSizeDataAccessException("creditProfileId=" +profileUiid + " creditProfileDao.update",1,updateCount);
                }
                
            } else {
            	//warning from telus pubsub
                //savePubSubWarning(profileUiid, warning, aTelusChannel);
            }
        }
    }

  
	private List<Attachments> encryptCreditProfileAttachments(List<Attachments> attachments) {
		if(attachments!=null) {
			try{
				for (Attachments attachment : attachments) {
					attachment.setContent(cryptoService.encrypt(attachment.getContent()));
				}
			} catch (Exception e) {
				throw new CryptoException(e);
			}		
		}
		return attachments;
	}

	/*
	 * Fix for race issue . 
	 * The read db( firestore) shall contain only a valid customer
	 * with creditprofile that is returned to consumers . a valid creditprofile has
	 * to have at least crediclass. Hence Svc api shall only publish the
	 * customer/creditprofile from postgress database that contains a valid
	 * creditprofile.
	 * 
	 */	
	private boolean shouldUpdateCustomerCollection(CustomerWithCreateUpdateFlag c) {
		long custId =0;
		boolean updateCustomerCollectionInd=false;	
		
		if(		c!=null && c.getCustomerFromDatabase()!=null &&
				c.getCustomerFromDatabase().getCreditProfile().size()>0) 
		{
			 TelusCreditProfile creditprofileFromDatabase = c.getCustomerFromDatabase().getCreditProfile().get(0);
			 custId = creditprofileFromDatabase.getRelatedPartyCustomerRoleCustId();
			 
			 String creditClassCd = (creditprofileFromDatabase.getCreditClassCd()!=null )?creditprofileFromDatabase.getCreditClassCd():"" ;
			 creditClassCd=(creditClassCd!=null)?creditClassCd.trim():"";
			 if(!creditClassCd.isEmpty()) {
				 updateCustomerCollectionInd=true;
			 }			
		}
		 LOGGER.info("CustId={}. shouldUpdateCustomerCollection ", custId,updateCustomerCollectionInd);
		 
	   return updateCustomerCollectionInd;
	}
	


	/**
	 * Patch credit profile and engagedParty. Update credit profiles if they exist (Id != null), create if they doesn't.
	 *
	 * @param customerEntity
	 * @param customerToPatch
	 * @return customer uid
	 */
	private String patchCustomer(CustomerEntity customerEntity, CustomerToPatch customerToPatch) {
		String custUid = customerEntity.getCreditProfileCustomerId();
		 LOGGER.info("CustId={}. Patch customer ", customerEntity.getCustomerId());

		validationService.validateForPatch(customerToPatch);

		long startPatch = System.currentTimeMillis();

		if (customerToPatch.getCreditProfile() != null) {
			LOGGER.info("Patching credit profile for customer {}", custUid);
			for (TelusCreditProfile telusCreditProfile : customerToPatch.getCreditProfile()) {
				creditProfileService.patchCreditProfileByCustId(customerEntity, telusCreditProfile);
			} 
		}

		
		LOGGER.info("Update customer {} took {}ms", custUid, System.currentTimeMillis() - startPatch);
		return custUid;
	}


	@Transactional
	@Override
	public Customer getCustomerPartyFromDatabase(Long ownerCustId) {
		//response
		Customer responseCustomer= new Customer();
		//get customer and party entities from database
		Optional<List<CompositeEntity>> custPartyCompositeEntityOptionals;
		try {
			custPartyCompositeEntityOptionals = customerDao.query(
																	new CompositeRowMapper(new CustomerRowMapper(), new PartyRowMapper())
																	, Queries.SELECT_CUSTOMER_PARTY
																	,ownerCustId);
		} catch (DataAccessException  e) {
			String errorMessage = "[CustId="+ownerCustId+"]" + e.getMessage() + " "; 
		    e.addSuppressed(new Exception(errorMessage));
		    throw e;
		}
		
		
		if (!custPartyCompositeEntityOptionals.isPresent()) {
			return null;
		}
		List<CompositeEntity> custPartyCompositeEntityList= custPartyCompositeEntityOptionals.get();
		for (CompositeEntity custPartyCompositeEntity : custPartyCompositeEntityList) {
		
				//CompositeEntity custPartyCompositeEntity = custPartyCompositeEntityOptional.get();
				
				CustomerEntity ownerCustomerEntity  = custPartyCompositeEntity.getEntity(0, CustomerEntity.class);
				PartyEntity ownerPartyEntity = custPartyCompositeEntity.getEntity(1, PartyEntity.class);
				//get complete engaged party (indiv or org)
				RelatedPartyInterface ownerCustRelatedPartyEngagedParty = engagedPartyService.getEngagedParty(ownerPartyEntity.getPartyId());
				
				RelatedParty ownerCustRelatedParty =new RelatedParty();
				ownerCustRelatedParty.setId(String.valueOf(ownerCustId));
				ownerCustRelatedParty.setRole(ownerCustomerEntity.getRole());			
				ownerCustRelatedParty.setType("Customer");			
				if(ownerCustRelatedPartyEngagedParty!=null) {
					RelatedPartyToPatch engagedPartyToPatch = mapEngagedPartyI_to_EngagedPartyToPatch(ownerCustRelatedPartyEngagedParty);
					ownerCustRelatedParty.setEngagedParty(engagedPartyToPatch);	
				}
		
				//get CreditProfiles, Warnings and Product Category codes
				List<TelusCreditProfile> creditProfiles = creditProfileService.getCreditProfiles_Warnings_And_CustomerRelation_ByCreditProfileCustomerId(ownerCustomerEntity.getCreditProfileCustomerId());
				
				//for each creditProfile get all the customer this creditProfile has a relationship with 
				for (TelusCreditProfile telusCreditProfile : creditProfiles) {
					
					//if telusCreditProfile is primary , add customerEntity to it as the relatedparty
					String creditProfileId= telusCreditProfile.getId();		
					String customerRelation = telusCreditProfile.getCustomerCreditProfileRelCd();
					
					//Populate all related customer relatedParty 
					telusCreditProfile.setRelatedParties(new ArrayList<RelatedParty>());
					//add customer relatedParty with PRI relation
					if(CreditMgmtCommonConstants.PRIMARY_CREDITPROFILE.equalsIgnoreCase(customerRelation)){
						telusCreditProfile.getRelatedParties().add(ownerCustRelatedParty);
					}
					//add  customer relatedParty with relation other than PRI 
					else {					
						Optional<List<CustomerEntity>> optionalCustomerEntities = customerDao.get_Customer_CustomerCreditProfileRel_ByCreditProfileId(creditProfileId);
						if(optionalCustomerEntities.isPresent()) {
							List<CustomerEntity> customerEntities = optionalCustomerEntities.get();				
							for (CustomerEntity aCustomerEntity : customerEntities) {
								Long relatedCustId = aCustomerEntity.getCustomerId();
								if(Objects.equals(relatedCustId, ownerCustId)) {
									RelatedParty relatedCustRelatedParty = new RelatedParty();
									relatedCustRelatedParty.setId(String.valueOf(relatedCustId));
									relatedCustRelatedParty.setRole(aCustomerEntity.getRole());			
									relatedCustRelatedParty.setType("Customer");			
									telusCreditProfile.getRelatedParties().add(relatedCustRelatedParty);
								}
							}
						}
					}
				}	
				
				CustomerEntity customerEntity=custPartyCompositeEntity.getEntity(0, CustomerEntity.class);
				responseCustomer.setId(Long.toString(customerEntity.getCustomerId()));
				if(responseCustomer.getCreditProfile()==null) {
					responseCustomer.setCreditProfile(new ArrayList<TelusCreditProfile>());
				}
				responseCustomer.getCreditProfile().addAll(creditProfiles);
					
		}		


		//return mapCustomerCreditProfiles(custPartyCompositeEntity.getEntity(0, CustomerEntity.class),creditProfiles);
		return responseCustomer;
	}
	
	 private RelatedPartyToPatch mapEngagedPartyI_to_EngagedPartyToPatch( RelatedPartyInterface iEngagedParty) {

		 RelatedPartyToPatch engagedPartyToPatch = new RelatedPartyToPatch();
		 if(iEngagedParty==null) {
			 return engagedPartyToPatch;
		 }
		 
	        if (PartyType.INDIVIDUAL.equals(iEngagedParty.getRelatedPartyType())) {
	        	Individual individual = (Individual) iEngagedParty;
	        	
	        	engagedPartyToPatch.setAtReferredType(individual.getAtReferredType());
	        	engagedPartyToPatch.setId(individual.getId());
	        	engagedPartyToPatch.setRole(individual.getRole()); 
	        	
	        	engagedPartyToPatch.setBirthDate(individual.getBirthDate());
	        	engagedPartyToPatch.setContactMedium(individual.getContactMedium());	        	
	        	engagedPartyToPatch.setIndividualIdentification(individual.getIndividualIdentification());
				engagedPartyToPatch.setCharacteristic(individual.getCharacteristic());

	        } else {
	        	if (PartyType.ORGANIZATION.equals(iEngagedParty.getRelatedPartyType())) {
		        	Organization organization = (Organization) iEngagedParty;
		        	engagedPartyToPatch.setAtReferredType(organization.getAtReferredType());
		        	engagedPartyToPatch.setId(organization.getId());        	
		        	engagedPartyToPatch.setRole(organization.getRole());  
		        	engagedPartyToPatch.setBirthDate(organization.getBirthDate());
		        	
		        	engagedPartyToPatch.setContactMedium(organization.getContactMedium());	        	
		        	engagedPartyToPatch.setOrganizationIdentification(organization.getOrganizationIdentification());
	        	}

	        	       	
	        }
	        
		return engagedPartyToPatch;
	}
	
	private Customer mapCustomerCreditProfiles(CustomerEntity customerEntity,List<TelusCreditProfile> creditProfiles) {
		Customer cust = new Customer();
		cust.setId(Long.toString(customerEntity.getCustomerId()));
		cust.setCustUid(customerEntity.getCreditProfileCustomerId());

		cust.setCreditProfile(creditProfiles);
		return cust;
	}
	
	 private class CustomerWithCreateUpdateFlag {
	      private Customer customerFromDatabase;
	      private String createUpdateFlag;

	      public CustomerWithCreateUpdateFlag(Customer c, String f) {
	         this.customerFromDatabase = c;
	         this.createUpdateFlag = f;
	      }

	      public Customer getCustomerFromDatabase() {
	         return customerFromDatabase;
	      }



	      public String getCreateUpdateFlag() {
	         return createUpdateFlag;
	      }


	   }


	    private void createCreditWarnings(String profileId, List<TelusCreditDecisionWarning> warnings,
                TelusCreditProfile creditProfile) {
			for (TelusCreditDecisionWarning warning : CommonHelper.nullSafe(warnings)){
				CreditWarningHistoryEntity entity = CreditWarningHistoryModelMapper.toEntity(warning);
				if (StringUtils.isBlank(warning.getId())) {
					validationService.validateForCreate(warning);
					LOGGER.info("Creating warning history for credit profile {}", profileId);
					
					String id = creditWarningHistoryDao.insert(entity
					  .creditProfileId(profileId)
					  .createdBy(creditProfile.getChannel().getUserId()).updatedBy(creditProfile.getChannel().getUserId())
					  .originatorAppId(creditProfile.getChannel().getOriginatorAppId())
					  .channelOrgId(creditProfile.getChannel().getChannelOrgId()));
					
					warning.setId(id);
				}
			}
	}
	 

	    @Override
		@Transactional
		public Customer createCreditProfileResource(
										   RequestContext requestContext, 
										   Long primaryCustomerId,
										   TelusCreditProfile incomingCreditProfile,  
										   AccountInfo accountInfo,
										   long receivedTime,
										   long submitterEventTime,
										   String eventDescription) {

	    	
	    	TransactionTemplate txTemplate = new TransactionTemplate(txManager);
			final Map.Entry<String, CustomerWithCreateUpdateFlag> customerInDB = txTemplate.execute
					(status -> {
									//STEP: store CreditProfile Resource in db( party, customer , creditprofile, relationship )
									String primaryCustomerDBId = persistNewCreditProfileResource(primaryCustomerId, incomingCreditProfile);

									//get  the complete creditprofile , customer  and party data from DB to be stored in firestore read db
									//STEP: get complete customer from db
									Customer customerFromDatabase = getCustomerPartyFromDatabase(primaryCustomerId);
									if (customerFromDatabase != null) {
										customerFromDatabase.setCustUid(primaryCustomerDBId);
									}
									
									//STEP:publish change audit doc to be stored in firestore
									responseInterceptorService.resolveMissingFieldsAndAudit(incomingCreditProfile, customerFromDatabase);
									CustomerWithCreateUpdateFlag c = new CustomerWithCreateUpdateFlag(customerFromDatabase, "C");
									return new AbstractMap.SimpleEntry<>(customerFromDatabase.getCustUid(), c);
							}
					);

			//STEP: publish message to read db ( firestore sync message) 
			publishFirestoreSyncCreditProfileMessage(
					incomingCreditProfile.getAttachments(), 
					accountInfo,
					receivedTime, submitterEventTime, eventDescription, customerInDB,incomingCreditProfile.getLineOfBusiness());

			//STEP: prepare the Response : decrypt and Resolve fields which were not stored in database
			responseInterceptorService.decryptCustomerFromDb(customerInDB.getValue().getCustomerFromDatabase());
			
		    //STEP: publish the incomingCreditProfile to world	
			creditProfileEventSender.publish(Collections.singletonList(incomingCreditProfile),"CreditProfileCreateEvent");

			return customerInDB.getValue().getCustomerFromDatabase();
		}

		private String persistNewCreditProfileResource(Long custId, TelusCreditProfile incommingCreditProfile) {
			//set customer-creditprofile relationship to Primary  
			incommingCreditProfile.setCustomerCreditProfileRelCd(CreditMgmtCommonConstants.PRIMARY_CREDITPROFILE);				
			//insert creditprofile 
			final String profileId = creditProfileDao.insert(TelusCreditProfileModelMapper.toCreditProfileEntity(incommingCreditProfile));
			
			//insert Prod Qual
			if(!CollectionUtils.isEmpty(incommingCreditProfile.getProductCategoryQualification())) {
				List<ProdQualEntity> prodQualEntities = ProdQualModelMapper.toEntity(profileId, incommingCreditProfile.getProductCategoryQualification(), incommingCreditProfile.getChannel());
				prodQualEntities.forEach(prodQualEntity -> prodQualDao.insert(prodQualEntity));
			}

			incommingCreditProfile.setId(profileId);			        
			//insert warnings 
			if (incommingCreditProfile.getWarningHistoryList() != null) {
			    createCreditWarnings(profileId, incommingCreditProfile.getWarningHistoryList(), incommingCreditProfile);
			}	        
						
			//insert all related customer and their engaged party and insert creditprofile customer(s) relationship 
			String relatedPartyCustomerRoleCustUid="";
			if(incommingCreditProfile.getRelatedParties()!=null) {
			 for (RelatedParty custRelatedParty : incommingCreditProfile.getRelatedParties()) { 	  
			    boolean isCustomerRole= ("customer".equalsIgnoreCase( custRelatedParty.getRole()));
			    if(isCustomerRole) {
			    	
			    		incommingCreditProfile.setCustomerCreditProfileRelCd(CreditMgmtCommonConstants.PRIMARY_CREDITPROFILE);
			    		//store party(indv/org) 
			    		String partyId =  null;
			    		if((custRelatedParty.getEngagedParty()!=null) ){
			    		 partyId = engagedPartyService.createEngagedParty(custRelatedParty.getEngagedParty(), incommingCreditProfile.getChannel());
			    		 custRelatedParty.getEngagedParty().setId(partyId);	
			    		}
			    		//store customer   
			    		TelusChannel telusChannel = incommingCreditProfile.getChannel();
			    		telusChannel=(telusChannel!=null)?telusChannel:new TelusChannel();

			    		Optional<CustomerEntity> customerEntity = customerDao.findCustomerEntityByIdForUpdate(custId,incommingCreditProfile.getLineOfBusiness());
			    		String customerUid="";
			    		
			    		if (!customerEntity.isPresent() || customerEntity.get().getCustomerId() == null) {			    			
							 customerUid = customerDao.insert(
									new CustomerEntity()
									.customerId(custId)
									.partyId(partyId)
									.createdBy(telusChannel.getUserId())
									.updatedBy(telusChannel.getUserId())
									.originatorAppId(telusChannel.getOriginatorAppId())
									.channelOrgId(telusChannel.getChannelOrgId())
									.lineOfBusiness(incommingCreditProfile.getLineOfBusiness())
									.role(custRelatedParty.getRole())
									);
			    		}else {
			    			 customerUid=customerEntity.get().getCreditProfileCustomerId();
			    		}
						//store creditprofile and customer relationship 	
			            customerCreditProfileRelDao.insert(new CustomerCreditProfileRelEntity()
			                    .creditProfileId(profileId)
			                    .creditProfileCustomerId(customerUid)
			                    .createdBy(incommingCreditProfile.getCreatedBy())
			                    .originatorAppId(incommingCreditProfile.getChannel().getOriginatorAppId())
			                    .channelOrgId(incommingCreditProfile.getChannel().getChannelOrgId())
			                    .customerCreditProfileRelCd(CreditMgmtCommonConstants.PRIMARY_CREDITPROFILE));	
		            			        		
			    		relatedPartyCustomerRoleCustUid=customerUid;
					if("WIRELINE".equals(incommingCreditProfile.getLineOfBusiness())) {
						//STEP: publish the incomingCreditProfile to MDM
						//mdmEventSender.publish(Collections.singletonList(incomingCreditProfile),"CreditProfileCreateEvent");
						if(Objects.nonNull(custRelatedParty.getEngagedParty())) {
							String message = mdmEventSender.createJSONMessageForCreditIds(custRelatedParty.getEngagedParty().getIndividualIdentification(), ""+custId, telusChannel.getUserId(),"CREATE");
							mdmEventSender.publish(message);
						}
					}
			    }else {
			    		//customer's role is not customer
						String customerUid = customerDao.insert(
								new CustomerEntity()
								.customerId(custId)
								//.partyId(partyId)
								.createdBy(incommingCreditProfile.getChannel().getUserId())
								.updatedBy(incommingCreditProfile.getChannel().getUserId())
								.originatorAppId(incommingCreditProfile.getChannel().getOriginatorAppId())
								.channelOrgId(incommingCreditProfile.getChannel().getChannelOrgId())
								.lineOfBusiness(incommingCreditProfile.getLineOfBusiness())
								.role(custRelatedParty.getRole())
								);			        		
			            customerCreditProfileRelDao.insert(new CustomerCreditProfileRelEntity()
			                    .creditProfileId(profileId)
			                    .creditProfileCustomerId(customerUid)
			                    .createdBy(incommingCreditProfile.getCreatedBy())
			                    .originatorAppId(incommingCreditProfile.getChannel().getOriginatorAppId())
			                    .channelOrgId(incommingCreditProfile.getChannel().getChannelOrgId())
			                    .customerCreditProfileRelCd(null));		        		
			    }
				
			}
		}
		return relatedPartyCustomerRoleCustUid;
		}




	private void publishFirestoreSyncCreditProfileMessage(
				List<Attachments> cpAttachments,
				AccountInfo accountInfo, 
				long receivedTime, 
				long submitterEventTime,
				String eventDescription, 
				final Map.Entry<String, 
				CustomerWithCreateUpdateFlag> customerInDB,
				String requestLineofbusiness) {
			TransactionTemplate txTemplate2 = new TransactionTemplate(txManager);
			//interface TransactionOperation
			//	void executeWithoutResult(Consumer<TransactionStatus> action) 
			//take status input param. status is of type Consumer<TransactionStatus> per interface TransactionOperation
			//returns void per method void executeWithoutResult(Consumer<TransactionStatus> action) in per interface TransactionOperation
			//Rather than making making the whole method transactional, we are using programmatic transaction management over the declarative approach.
			//another reason to use programmatic transaction management, is to trigger rollack given by default, rollback happens for runtime, unchecked exceptions only. The checked exception does not trigger a rollback of the transaction. 
			txTemplate2.executeWithoutResult(transactionStatus -> 
			{				
				try {
					//UPDATE readdb_sync_status SET 
					readDbSyncStatusDao.update(customerInDB.getKey(), new ReaddbSyncStatusEntity().needToSync(false));
					
					CustomerWithCreateUpdateFlag c = customerInDB.getValue();
					//fix for race condition issue
					//if(shouldUpdateCustomerCollection(c))
					{
						//encrypt creditProfile attachments to be stored in firestore collection
						List<TelusCreditProfile> creditProfileList = c.getCustomerFromDatabase().getCreditProfile();
						for (TelusCreditProfile creditProfile : creditProfileList) {
							if(requestLineofbusiness!=null && requestLineofbusiness.equalsIgnoreCase(creditProfile.getLineOfBusiness()) ){
								creditProfile.setAttachments(encryptCreditProfileAttachments(cpAttachments));
							}
						}
						//updateCustomerCollection
						readDB.updateCustomerCollection(c.getCustomerFromDatabase(), accountInfo, c.getCreateUpdateFlag(), receivedTime,submitterEventTime,eventDescription);
					}
				} catch (Exception e) {
					String custId = 
							(customerInDB!=null && customerInDB.getValue()!=null && customerInDB.getValue().getCustomerFromDatabase()!=null)?
							customerInDB.getValue().getCustomerFromDatabase().getId()
							:"empty custId";
										
					if (e instanceof PubSubPublishException) {
						LOGGER.error("{}: CustId={} . {} Error publishing sync message. {} ", ExceptionConstants.STACKDRIVER_METRIC, custId,  ExceptionConstants.PUBSUB201, ExceptionHelper.getStackTrace(e));
					} else if (e instanceof DataAccessException) {
						LOGGER.error("{}: CustId={} .{} Database error. {} ", ExceptionConstants.STACKDRIVER_METRIC, custId,ExceptionConstants.POSTGRES100, ExceptionHelper.getStackTrace(e));
					} else {
						LOGGER.error("{} CustId={} .Error publishing sync message. {} ", ExceptionConstants.STACKDRIVER_METRIC, custId,ExceptionHelper.getStackTrace(e));
					}
					transactionStatus.setRollbackOnly();
				}
			}
			);
		}
		
		
	 
}
