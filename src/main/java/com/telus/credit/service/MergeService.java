package com.telus.credit.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.telus.credit.common.CreditProfileConstant;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.dao.CproflMappingDao;
import com.telus.credit.dao.CreditProfileDao;
import com.telus.credit.dao.CustomerCreditProfileRelDao;
import com.telus.credit.dao.CustomerDao;
import com.telus.credit.dao.PartyDao;
import com.telus.credit.dao.entity.CproflMappingEntity;
import com.telus.credit.dao.entity.CreditProfileEntity;
import com.telus.credit.dao.entity.CustomerCreditProfileRelEntity;
import com.telus.credit.dao.entity.CustomerEntity;
import com.telus.credit.dao.entity.PartyEntity;
import com.telus.credit.exceptions.CreditException;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.exceptions.PubSubPublishException;
import com.telus.credit.firestore.model.CreditAssessment;
import com.telus.credit.model.Attachments;
import com.telus.credit.model.ContactMedium;
import com.telus.credit.model.CreditProfileAuditDocument;
import com.telus.credit.model.Customer;
import com.telus.credit.model.ProductCategoryQualification;
import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCharacteristic;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.model.TelusIndividualIdentification;
import com.telus.credit.model.TimePeriod;
import com.telus.credit.model.common.IdentificationType;
import com.telus.credit.model.common.PartyType;
import com.telus.credit.model.mapper.TelusCreditProfileModelMapper;
import com.telus.credit.pubsub.model.CreditAssessmentEvent;
import com.telus.credit.pubsub.service.TelusCreditProfileEventSender;
import com.telus.credit.service.impl.AuditService;
import com.telus.credit.service.impl.CreditAssessmentMessageSender;
import com.telus.credit.service.impl.CustomerCollectionService;
import com.telus.credit.service.impl.ResponseInterceptorService;

@Service
public class MergeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeService.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private CustomerCollectionService readDB;

	 @Autowired
	 private CryptoService cryptoService;  
	  
	@Autowired
	private CustomerCreditProfileRelDao customerCreditProfileRelDao;	 
	
	
	@Autowired
    private CreditProfileDao creditProfileDao;

    @Autowired
    private CustomerDao customerDao;

	@Autowired
	private ResponseInterceptorService responseInterceptorService;
	
	@Autowired
	private PartyDao partyDao;

	@Autowired
	private CproflMappingDao cproflMappingDao;

	@Autowired
	private TelusCreditProfileEventSender creditProfileEventSender;
	
    @Autowired
    private CreditAssessmentMessageSender messageSender;

    public static String UPDATED_BY_MERGE 		= "MERGE";
	public static String MERGED_STATUS_CD 		= "C";
	public static String MERGED_MAPPING_TYPE_CD = "MI" ;
	public static String MERGED_EVENT_DESC		="Merge Event"  ;
	
    
	/*
	 * Query db to find customers that had one of the following changes today: creditclass change, party'dob change, party identification change). 
	 * For each changed customerId, get the customer(including DOB,cardIDs,CreditValue) perform hardmatch, perform merge,,...)    
     */
    public void searchAndMergeCreditProfiles(com.telus.credit.pubsub.model.MergeMessage mergeMessage, long submitterEventTime,long receivedTime) {
    	LOGGER.info("Start MergeService searchAndMergeCreditProfiles ");
    	//STEP get list of customers that had a change today.
    	Optional<List<Long>> changedCustomerIdListOptional = creditProfileDao.getTodayChangedCustomersCandidateForMerging();
    	List<Long> changedCustomerIdList = changedCustomerIdListOptional.get();
		if( changedCustomerIdList.isEmpty() ) {
			LOGGER.info("No TodayChangedCustomersCandidateForMerging ");
			return;
		} 
		LOGGER.info("changedCustomerIdListOptional = {} ",changedCustomerIdListOptional);
		//STEP for each customer, perform merge Matching CreditProfiles
    	for (Long custId : changedCustomerIdList) {
        	Customer machedCustomer = customerService.getCustomerPartyFromDatabase(custId);
        	List<TelusCreditProfile> filtereByLOB_DbCreditProfileListList = machedCustomer.getCreditProfile().stream()
    	            .filter(entity -> entity.getLineOfBusiness().equals("WIRELINE"))
    	            .collect(Collectors.toList());         	
        	TelusCreditProfile inputCreditprofile = getPrimaryCreditProfile(filtereByLOB_DbCreditProfileListList);    
    		if(inputCreditprofile!=null && validForMerge(inputCreditprofile)) {     	
    			mergeMatchingCreditProfiles( inputCreditprofile, submitterEventTime, receivedTime);			
    		}
		}
    }
    
    public void mergeMatchingCreditProfiles(TelusCreditProfile inputCreditprofile,long submitterEventTime,long receivedTime) {
		List<Long> matchedCustomerIdList = findCustomerIdsMatchingMergeCriteria(inputCreditprofile);
		if( matchedCustomerIdList.isEmpty() || matchedCustomerIdList.size()==1) {
			LOGGER.info("CustId={} no matching credirpofile to merge found ", inputCreditprofile.getCustomerRelatedParty().getId());
			return;
		}	
		
		List<TelusCreditProfile> machedCreditprofiles = matchByCreditClass(inputCreditprofile, matchedCustomerIdList);		
		if( machedCreditprofiles.isEmpty() || machedCreditprofiles.size()==1) {
			LOGGER.info("CustId={} no matching(by creditclass) credirpofile to merge found ", inputCreditprofile.getCustomerRelatedParty().getId());
			return;
		}
		
		LOGGER.info("CustId={}  , machedCreditprofile to merges = {}", inputCreditprofile.getCustomerRelatedParty().getId(),machedCreditprofiles);
		//sort Creditprofiles by UpdatedTs date descending
		machedCreditprofiles.sort(Comparator.comparing(TelusCreditProfile::getUpdatedTs).reversed());
		
		//select activeCP. 
		 TelusCreditProfile activeCreditprofile = machedCreditprofiles.get(0);
		 LOGGER.info("CustId={} , Select active creditprofile's custId ", activeCreditprofile.getCustomerRelatedParty().getId(), activeCreditprofile.getCustomerRelatedParty().getId());
		 machedCreditprofiles.remove(0);		 
		 
		 //remove already merged creditprofiles. these are machedCreditprofiles  with same creditprofileId as active creditprofileId
		 for (Iterator<TelusCreditProfile> iterator = machedCreditprofiles.iterator(); iterator.hasNext();) {
			 TelusCreditProfile machedCreditprofile = (TelusCreditProfile) iterator.next();
			 if( machedCreditprofile.getId().equalsIgnoreCase(activeCreditprofile.getId()) ){
				 iterator.remove();
			 }			 
		 }
		if( machedCreditprofiles.isEmpty()) {
			LOGGER.info("CustId={}  there are no credirpofile to merge", inputCreditprofile.getCustomerRelatedParty().getId());
			return;
		}     

TransactionTemplate txTemplate = new TransactionTemplate(txManager);
txTemplate.executeWithoutResult(transactionStatus -> 
{
	try {
		//consolidateContent
		consolidateContent(machedCreditprofiles,activeCreditprofile);
		
		//update activeCreditprofile in DB
		try {
			//decrypt CreditProfile in order to reuse operation updateCreditProfileResourceByCPId .
			responseInterceptorService.decryptCreditProfileFromDb(activeCreditprofile);	
			customerService.updateCreditProfileResourceByCPId(null, activeCreditprofile.getId(), activeCreditprofile, null, receivedTime, submitterEventTime, "Merge Event");
		} catch (Exception e) {
			LOGGER.error("CustId={} {} updateCreditProfileResourceByCPId Exception. {}", activeCreditprofile.getRelatedParties().get(0).getId(),ExceptionConstants.STACKDRIVER_METRIC, ExceptionHelper.getStackTrace(e));
			throw e;
		}		
		
		//consolidate links: update each matchedCreditprofile to point to activeCreditprofile entities
		String activeCreditProfileId = activeCreditprofile.getId();
		String activePartyId    = activeCreditprofile.getRelatedParties().get(0).getEngagedParty().getId();

		
		for (TelusCreditProfile machedCreditprofile : machedCreditprofiles) {
			//update customer table. change mergedCustomer's partyID to activeCustomer's PartyID
			RelatedParty mergedCustomer = machedCreditprofile.getRelatedParties().get(0);			
			String machedCreditProfileId = machedCreditprofile.getId();
			
			//update merged customer's partyID to active customer's partyID
			Optional<CustomerEntity> dbMergedCustomerEntityOptional = customerDao.findCustomerEntityByIdForUpdate( new Long(mergedCustomer.getId()), "WIRELINE");
			CustomerEntity dbMergedCustomerEntity = dbMergedCustomerEntityOptional.get();
			String dbMergedCreditProfileCustomerId = dbMergedCustomerEntity.getCreditProfileCustomerId();
			dbMergedCustomerEntity.setPartyId(activePartyId);
			dbMergedCustomerEntity.setUpdatedBy(UPDATED_BY_MERGE);
			dbMergedCustomerEntity.setCreatedTs(Timestamp.from(Instant.now()));
			customerDao.update(dbMergedCustomerEntity.getCreditProfileCustomerId(), dbMergedCustomerEntity);

			//update customer_credit_profile_rel. change mergedCusotmer;s credit_profile_id to activeCustomer's credit_profile_id
			 Optional<CustomerCreditProfileRelEntity> dbCustomerCreditProfileRelEntityOptional = customerCreditProfileRelDao.getCustomerCreditProfileRel_By_CreditProfileCustomerId(dbMergedCreditProfileCustomerId);
			 CustomerCreditProfileRelEntity dbCustomerCreditProfileRelEntity = dbCustomerCreditProfileRelEntityOptional.get();
			 dbCustomerCreditProfileRelEntity.setCreditProfileId(activeCreditProfileId);
			 dbCustomerCreditProfileRelEntity.setUpdatedOnTs(new Timestamp(System.currentTimeMillis()));
			 dbCustomerCreditProfileRelEntity.setUpdatedBy(UPDATED_BY_MERGE);			 
			 customerCreditProfileRelDao.update(dbCustomerCreditProfileRelEntity.getCreditProfileCustomerId(), dbCustomerCreditProfileRelEntity);
			
	
			// update status code of mergedCreditprofile from A "Active" to C "Consolidate"			
			 if(!machedCreditProfileId.equalsIgnoreCase(activeCreditProfileId)) {
				Optional<CreditProfileEntity> dbMergedCreditProfileEntityOptional = creditProfileDao.getByCreditProfileUid(machedCreditProfileId);
				CreditProfileEntity dbMergedCreditProfileEntity = dbMergedCreditProfileEntityOptional.get();	
				
				dbMergedCreditProfileEntity.setCreditProfileStatusCd(MERGED_STATUS_CD);
				dbMergedCreditProfileEntity.setCreditProfileStatusTs(""+DateTimeUtils.toUtcString(new Timestamp(System.currentTimeMillis())));  				
				dbMergedCreditProfileEntity.setUpdatedBy(UPDATED_BY_MERGE);	
				dbMergedCreditProfileEntity.setUpdatedTs(new Timestamp(System.currentTimeMillis())); 
				LOGGER.info("CustId={}  updating creditProfile ", mergedCustomer.getId());
				creditProfileDao.update(dbMergedCreditProfileEntity.getCreditProfileId(),dbMergedCreditProfileEntity);

				//update status code of mergedParty from to C "Consolidate"
				Optional<PartyEntity> dbPartyEntityOptional = partyDao.getById(dbMergedCustomerEntity.getPartyId());
				PartyEntity dbPartyEntity = dbPartyEntityOptional.get();
				dbPartyEntity.setStatusCd(MERGED_STATUS_CD);
				dbPartyEntity.setUpdatedTs(new Timestamp(System.currentTimeMillis()));  
				dbPartyEntity.setUpdatedBy(UPDATED_BY_MERGE);
				LOGGER.info("CustId={}, PartyId={} updating merged Party", mergedCustomer.getId(),dbPartyEntity.getPartyId());
				partyDao.update(dbPartyEntity.getPartyId(), dbPartyEntity);

				//maintainCreditProfile : create entry in crediprofile mapping history table (from crediprofile , to crediprofile)
				CproflMappingEntity cprofileMappingEntity = new CproflMappingEntity();	
				cprofileMappingEntity.setCustomerId(new Long(mergedCustomer.getId()));
				cprofileMappingEntity.setCproflFromId(dbMergedCreditProfileEntity.getCreditProfileId());
				cprofileMappingEntity.setCproflToId(activeCreditProfileId);				
				cprofileMappingEntity.setCproflMappingTypCd(MERGED_MAPPING_TYPE_CD);
				cprofileMappingEntity.setCreatedOnTs(Timestamp.from(Instant.now()));
				cproflMappingDao.insert(cprofileMappingEntity);

				//publish firestore sync message	
				LOGGER.info("CustId={}, publish firestore events for  updated machedCreditprofile  ",mergedCustomer.getId());
				Customer updatedMergedCustomer = customerService.getCustomerPartyFromDatabase(new Long(mergedCustomer.getId()));
				try {										
					readDB.updateCustomerCollection(updatedMergedCustomer, null, "U", receivedTime,submitterEventTime,MERGED_EVENT_DESC);
				} catch (PubSubPublishException e) {
					if (e instanceof PubSubPublishException) {
						LOGGER.error("CustId={}, {}: {} Error publishing sync message. {} ", mergedCustomer.getId(),ExceptionConstants.STACKDRIVER_METRIC,  ExceptionConstants.PUBSUB201, ExceptionHelper.getStackTrace(e));
					} else {
						LOGGER.error("CustId={}, {} Error publishing sync message. {} ", mergedCustomer.getId(),ExceptionConstants.STACKDRIVER_METRIC, ExceptionHelper.getStackTrace(e));
					}
				}
				//STEP:publish change audit doc to be stored in firestore
				CreditProfileAuditDocument auditDocument = AuditService.auditContext();
				//auditDocument.setCorrelationId(requestContext.getCorrId());
				auditDocument.setCustomerId(Objects.toString(mergedCustomer.getId()));
				auditDocument.setChannelOrgId(UPDATED_BY_MERGE);
				auditDocument.setOriginatingAppId(UPDATED_BY_MERGE);
				auditDocument.setUserId(UPDATED_BY_MERGE);		
				auditDocument.setEventTimestamp(Timestamp.from(Instant.now()));
				auditDocument.setEventType(CreditProfileAuditDocument.EventType.CP_UPDATE);		
				responseInterceptorService.resolveMissingFieldsAndAudit(machedCreditprofile,updatedMergedCustomer);
				

				//STEP: publish the incomingCreditProfile to world	
				responseInterceptorService.decryptCustomerFromDb(updatedMergedCustomer);			    
				creditProfileEventSender.publish(Collections.singletonList(updatedMergedCustomer.getCreditProfile().get(0)),"CreditProfileCreateEvent");				
			 }
			
		}
	}catch (Exception e) {
		LOGGER.error("{} update transaction failed . {} ", ExceptionConstants.STACKDRIVER_METRIC, ExceptionHelper.getStackTrace(e));
		transactionStatus.setRollbackOnly();
	}		
});		
	
	}

	private List<TelusCreditProfile> matchByCreditClass(TelusCreditProfile inputCreditprofile,
			List<Long> matchedCustomerIdList) {
		//List<Customer> machedCustomers= new ArrayList<Customer>();
		List<TelusCreditProfile> machedCreditprofiles= new ArrayList<TelusCreditProfile>();
		

		//include only creditprofiles with matching creditclasscd
		String  inputCreditClassCd= cryptoService.decryptAndIgnoreError(inputCreditprofile.getCreditClassCd());	
		for (Iterator<Long> iterator = matchedCustomerIdList.iterator(); iterator.hasNext();) {
			Long matchedCustomerId = (Long) iterator.next();
			Customer machedCustomer = customerService.getCustomerPartyFromDatabase(matchedCustomerId); 	
			if(machedCustomer!=null && machedCustomer.getCreditProfile()!=null) {
				List<TelusCreditProfile> filtereByLOB_DbCreditProfileListList = machedCustomer.getCreditProfile().stream()
					    .filter(entity -> entity != null && "WIRELINE".equals(entity.getLineOfBusiness()))
					    .collect(Collectors.toList());        			
	        	TelusCreditProfile machedCreditprofile = getPrimaryCreditProfile(filtereByLOB_DbCreditProfileListList); 	
	        	if(machedCreditprofile!=null) {
					String  matchedCreditClassCd= cryptoService.decryptAndIgnoreError(machedCreditprofile.getCreditClassCd());	
					if(inputCreditClassCd.equalsIgnoreCase(matchedCreditClassCd) ){
						machedCreditprofiles.add(machedCreditprofile);	
						LOGGER.info("Matched CustomerID={} credirpofileID={} ", machedCustomer.getId(), machedCreditprofiles.get(0).getId());
					}
	        	}
			}
		}
		return machedCreditprofiles;
	}

	public List<Long> findCustomerIdsMatchingMergeCriteria(TelusCreditProfile inputCreditprofile) {
		RelatedParty inputCustomer = inputCreditprofile.getCustomerRelatedParty();
		List<TelusIndividualIdentification> individualIdentification = inputCustomer.getEngagedParty().getIndividualIdentification();
		
		//ID values are stored in hashvalue.
		
    	String inputDL =getIdentificationIdHashed(individualIdentification,IdentificationType.DL.name());
    	String inputSin=getIdentificationIdHashed(individualIdentification,IdentificationType.SIN.name());
    	String inputCC =getIdentificationIdHashed(individualIdentification,IdentificationType.CC.name());
    	String inputPSP=getIdentificationIdHashed(individualIdentification,IdentificationType.PSP.name());
    	String inputPRV=getIdentificationIdHashed(individualIdentification,IdentificationType.PRV.name());
    	String inputHC =getIdentificationIdHashed(individualIdentification,IdentificationType.HC.name());		
		
    	String inputDOB =  (inputCustomer.getEngagedParty().getBirthDate()!=null)?inputCustomer.getEngagedParty().getBirthDate().trim():"";
    	
    	//search db to find merge candidates. Excludes any already merged(consolidated) Creditprofiles.
		Optional<List<Long>> matchedCustomerIdListOptional = creditProfileDao.findCustomerIdsMatchingMergeCriteria(inputDL,inputSin,inputCC,inputPSP,inputPRV,inputHC,inputDOB);
		if(matchedCustomerIdListOptional.isPresent()) {
			return matchedCustomerIdListOptional.get();
		}
		return new ArrayList<Long>();
	}


	public void consolidateContent(List<TelusCreditProfile> machedCreditprofiles,TelusCreditProfile activeCreditProfile) {

		
		//consolidate Address
		List<ContactMedium> activeCustomerContactMediumList = activeCreditProfile.getCustomerRelatedParty().getEngagedParty().getContactMedium();
		ContactMedium  activeCustomerPostalAddress= getContactMediumByType(activeCustomerContactMediumList,"postalAddress");		
		if(activeCustomerPostalAddress==null) {
			if(!"N".equalsIgnoreCase(activeCreditProfile.getCreditProfileConsentCd()) ){
				ContactMedium cm = getLatestPostalAddress(machedCreditprofiles);
				if(cm!=null ) {
					activeCustomerContactMediumList.add(cm);
				}
			}
		}
		
		//Update active creditprofile’s individual's CardIDs if its CardIDs value are null			 
        if (PartyType.INDIVIDUAL.getType().equalsIgnoreCase(activeCreditProfile.getCustomerRelatedParty().getEngagedParty().getAtReferredType())) {        	
         RelatedPartyToPatch activeCustomerEngagedParty = activeCreditProfile.getCustomerRelatedParty().getEngagedParty();
         List<TelusIndividualIdentification> activeCustomerIndividualIdentificationList = (activeCustomerEngagedParty!=null)?activeCustomerEngagedParty.getIndividualIdentification():new ArrayList<TelusIndividualIdentification>();    		 
         //
 
         IdentificationType[] typList = IdentificationType.values();
         for (int i = 0; i <typList.length; i++) {
    		 if( getIdentification(activeCustomerIndividualIdentificationList,typList[i].name())==null ) {
    			 TelusIndividualIdentification mergedIdentification= getLatestMergedIdentification(machedCreditprofiles,typList[i].name());
    			 if(mergedIdentification!=null) {
    				 activeCustomerIndividualIdentificationList.add(mergedIdentification );
    			 }
    		 }
        } 
 /*        
         String[] typList = {"DL","SIN","CC","PRV","PSP","HC"};
         for (int i = 0; i < typList.length; i++) {
    		 if( getIdentification(activeCustomerIndividualIdentificationList,typList[i])==null ) {
    			 TelusIndividualIdentification mergedIdentification= getLatestMergedIdentification(machedCreditprofiles,typList[i]);
    			 if(mergedIdentification!=null) {
    				 activeCustomerIndividualIdentificationList.add(mergedIdentification );
    			 }
    		 }			
		}
*/
 		/*
 		 * Update active creditprofile’s individual DOB if it is null
 		 */	
         RelatedParty activeCustRelatedParty = activeCreditProfile.getCustomerRelatedParty();
         String activeCustdob = (activeCustRelatedParty!=null && activeCustRelatedParty.getEngagedParty()!=null)?activeCustRelatedParty.getEngagedParty().getBirthDate():"";
	   	 if( activeCustdob.isEmpty() ) {	   			
	   			activeCustRelatedParty.getEngagedParty().setBirthDate(getLatestMergedDOB(machedCreditprofiles));
	   	 }
		//consolidate EmploymentStatusCode, PrimaryCreditCardCode ,SecondaryCreditCardCode,UnderLegalCareCode,RESIDENCY_CD
         String[] characteristicsCdList = {
        		 	CreditProfileConstant.EMPLOYMENT_STATUS_CD,
        		 	CreditProfileConstant.LEGAL_CARE_CD, 
        		 	CreditProfileConstant.PRIM_CRED_CARD_TYP_CD,
        		 	CreditProfileConstant.RESIDENCY_CD,
        		 	CreditProfileConstant.SEC_CRED_CARD_ISS_CO_TYP_CD};
         for (int i = 0; i < characteristicsCdList.length; i++) {	   	 
	   	  TelusCharacteristic activeCustCharacteristic = getEngagedPartyCharacteristics(activeCustomerEngagedParty.getCharacteristic(),characteristicsCdList[i]);
		  if(activeCustCharacteristic==null || activeCustCharacteristic.getValue()==null || activeCustCharacteristic.getValue().isEmpty()) {
		   		TelusCharacteristic mergedCharacteristics = getLatestMergedCharacteristics(machedCreditprofiles,characteristicsCdList[i]);
		   		if(mergedCharacteristics!=null) {
		   			activeCustomerEngagedParty.getCharacteristic().add(mergedCharacteristics);
		   		}
		  }
         }

        }
        			
		/*
		 * Update active creditprofile’s following attrs if active’s value is NA
		 * CreditCheckConsentCode, EmploymentStatusCode, PrimaryCreditCardCode, SecondaryCreditCardCode, UnderLegalCareCode,ApplicationProvinceCd
		 */
		//consolidate individual CreditCheckConsentCd        
		if(activeCreditProfile.getCreditProfileConsentCd()==null ||activeCreditProfile.getCreditProfileConsentCd().isEmpty() || "NA".equalsIgnoreCase(activeCreditProfile.getCreditProfileConsentCd())){
			activeCreditProfile.setCreditProfileConsentCd(getLatestMergedCreditCheckConsentCd(machedCreditprofiles));
		}       

		//consolidate ApplicationSubProvCd        
		if(activeCreditProfile.getApplicationProvinceCd()==null ||activeCreditProfile.getApplicationProvinceCd().isEmpty() ){
			activeCreditProfile.setApplicationProvinceCd(getLatestValidApplicationSubProvCd(machedCreditprofiles));
		}  			

		//consolidate ProductCatQualification and boltonind
		if( activeCreditProfile.getProductCategoryQualification()==null ||activeCreditProfile.getProductCategoryQualification().isEmpty() ){
			TelusCreditProfile cplWothPqual = getLatestNonEmptyProductCategoryQualification(machedCreditprofiles);	
			if(cplWothPqual!=null ) {
				activeCreditProfile.setProductCategoryQualification(cplWothPqual.getProductCategoryQualification());
				activeCreditProfile.setBoltonInd(cplWothPqual.getBoltonInd());
			}
		}  		
	}


	private TelusCreditProfile getLatestNonEmptyProductCategoryQualification( List<TelusCreditProfile> sortedMachedCreditprofiles) {
		for (TelusCreditProfile machedCreditprofile : sortedMachedCreditprofiles) {
			List<ProductCategoryQualification> productCategoryQualifications = machedCreditprofile.getProductCategoryQualification();
			if(productCategoryQualifications!=null && !productCategoryQualifications.isEmpty()) {
				return machedCreditprofile;
			}			
		}
		return null;
	}

	private TelusCharacteristic getLatestMergedCharacteristics(List<TelusCreditProfile> machedCreditprofiles, String cd) {
		for (TelusCreditProfile machedCreditprofile : machedCreditprofiles) {
			 List<TelusCharacteristic> mergedCustomerTelusCharacteristicList = machedCreditprofile.getCustomerRelatedParty().getEngagedParty().getCharacteristic();
	   		 for (TelusCharacteristic characteristic : mergedCustomerTelusCharacteristicList) {
	    			if(cd.equalsIgnoreCase(characteristic.getName()) ){
	    				return characteristic;
	    			}
	 		}			
		}
		return null;
	}
	private TelusCharacteristic getEngagedPartyCharacteristics(List<TelusCharacteristic> characteristicList, String cd) {
		for (TelusCharacteristic charcs : characteristicList) {
			if(cd.equalsIgnoreCase(charcs.getName())) {
				return charcs;
			}
		}
		return null;
	}

	private ContactMedium getContactMediumByType(List<ContactMedium> contactMediumList, String mediumtyp) {
		for (ContactMedium contactMedium : contactMediumList) {
			if(mediumtyp.equalsIgnoreCase(contactMedium.getMediumType())) {
				return contactMedium;
			}
		}
		return null;
	}
	private TelusIndividualIdentification getIdentification(List<TelusIndividualIdentification> customerIndividualIdentificationList,String idType) {
	   		 for (TelusIndividualIdentification customerIdentification : customerIndividualIdentificationList) {
	    			if(idType.equalsIgnoreCase(customerIdentification.getIdentificationType()) ){
	    				return customerIdentification;
	    			}
	 		}
			return null;			
	}


	private String getLatestValidApplicationSubProvCd(List<TelusCreditProfile> machedCreditprofiles) {
		for (TelusCreditProfile machedCreditprofile : machedCreditprofiles) {
			String mergedApplicationProvinceCd = (machedCreditprofile!=null && machedCreditprofile.getApplicationProvinceCd()!=null)?machedCreditprofile.getApplicationProvinceCd().trim():"";
			
			mergedApplicationProvinceCd=(!"NA".equalsIgnoreCase(mergedApplicationProvinceCd))?mergedApplicationProvinceCd:"";
			if(!mergedApplicationProvinceCd.isEmpty()) {
				return mergedApplicationProvinceCd.trim();
			}			
		}
		return "";
	}	
	private String getLatestMergedDOB(List<TelusCreditProfile> machedCreditprofiles) {
		for (TelusCreditProfile machedCreditprofile : machedCreditprofiles) {
			String mergedDOB = machedCreditprofile.getCustomerRelatedParty().getEngagedParty().getBirthDate();
			if(mergedDOB!=null && !mergedDOB.isEmpty() ) {
				return mergedDOB.trim();
			}			
		}
		return "";
	}
	private String getLatestMergedCreditCheckConsentCd(List<TelusCreditProfile> machedCreditprofiles) {
		for (TelusCreditProfile machedCreditprofile : machedCreditprofiles) {
			String mergedConsentCd = machedCreditprofile.getCreditProfileConsentCd();
			if(mergedConsentCd!=null && !mergedConsentCd.isEmpty() || !"NA".equalsIgnoreCase(mergedConsentCd)) {
				return mergedConsentCd.trim();
			}			
		}
		return "";
	}
	private TelusIndividualIdentification getLatestMergedIdentification(List<TelusCreditProfile> machedCreditprofiles,String idType) {
		for (TelusCreditProfile machedCreditprofile : machedCreditprofiles) {
			 List<TelusIndividualIdentification> mergedCustomerIndividualIdentificationList = machedCreditprofile.getCustomerRelatedParty().getEngagedParty().getIndividualIdentification();
	   		 for (TelusIndividualIdentification customerIdentification : mergedCustomerIndividualIdentificationList) {
	    			if(idType.equalsIgnoreCase(customerIdentification.getIdentificationType()) ){
	    				return customerIdentification;
	    			}
	 		}			
		}
		return null;
	}	
	private TelusCreditProfile getPrimaryCreditProfile(Customer customer) {
		List<TelusCreditProfile> cplist = customer.getCreditProfile();
		for (TelusCreditProfile cp : cplist) {
			if("PRI".equalsIgnoreCase(cp.getCustomerCreditProfileRelCd()) ){
				return cp;
			} 			
		}
		return null;
	}
	private TelusCreditProfile getPrimaryCreditProfile(List<TelusCreditProfile> cplist) {
		for (TelusCreditProfile cp : cplist) {
			if("PRI".equalsIgnoreCase(cp.getCustomerCreditProfileRelCd()) ){
				return cp;
			} 			
		}
		return null;
	}	
	//mergedcustomers is already sorted by creditprofile timestamp
	private ContactMedium getLatestPostalAddress(List<TelusCreditProfile> machedCreditprofiles) {
		for (TelusCreditProfile machedCreditprofile : machedCreditprofiles) {
			List<ContactMedium> contactMediumList =machedCreditprofile.getCustomerRelatedParty().getEngagedParty().getContactMedium();
			if(contactMediumList!=null && !contactMediumList.isEmpty()) {
				ContactMedium  customerPostalAddress= getContactMediumByType(contactMediumList,"postalAddress");
				if(customerPostalAddress!=null ) {
					return customerPostalAddress;
				}
			}
		}
		return null;
	}
	/*
	 * inputCreditprofile must have DOB ,creditvalue and at least one of the CardID
	 */
    public  boolean validForMerge(TelusCreditProfile inputCreditprofile) {
    	RelatedParty customer = inputCreditprofile.getCustomerRelatedParty();
    	List<TelusIndividualIdentification> individualIdentification = customer.getEngagedParty().getIndividualIdentification();

    	String inputDL =getIdentificationId(individualIdentification,IdentificationType.DL.name());
    	String inputSin=getIdentificationId(individualIdentification,IdentificationType.SIN.name());
    	String inputCC =getIdentificationId(individualIdentification,IdentificationType.CC.name());
    	String inputPSP=getIdentificationId(individualIdentification,IdentificationType.PSP.name());
    	String inputPRV=getIdentificationId(individualIdentification,IdentificationType.PRV.name());
    	String inputHC =getIdentificationId(individualIdentification,IdentificationType.HC.name());	    	
    	boolean minCardIdExist=false;
    	
    	if ( !ObjectUtils.isEmpty(inputDL)  || !ObjectUtils.isEmpty(inputSin) || !ObjectUtils.isEmpty(inputCC)  || !ObjectUtils.isEmpty(inputPSP)   || !ObjectUtils.isEmpty(inputPRV) || !ObjectUtils.isEmpty(inputHC)  ) {
    		minCardIdExist= true;
    	}
    	String inputCreditClassCd= (inputCreditprofile.getCreditClassCd()!=null)?inputCreditprofile.getCreditClassCd().trim():"";	
    	String birthDate =  (customer.getEngagedParty().getBirthDate()!=null)?customer.getEngagedParty().getBirthDate().trim():"";
	
    	if(!inputCreditClassCd.isEmpty() && !birthDate.isEmpty() && minCardIdExist ) {
    		return true;
    	}    	
    	
    	LOGGER.info("CustId={} skipped as isNotvalidForMerge", inputCreditprofile.getCustomerRelatedParty().getId());
		return false;
	}
    private String getIdentificationId(List<TelusIndividualIdentification> individualIdentification, String typ) {
    	if(individualIdentification==null || individualIdentification.isEmpty()){
    		return "";
    	}
    	for (TelusIndividualIdentification telusIndividualIdentification : individualIdentification) {	
    		if( typ.equals(telusIndividualIdentification.getIdentificationType()) ){		
    			String rslt = (telusIndividualIdentification.getIdentificationId()!=null)?telusIndividualIdentification.getIdentificationId().trim():"";
    			return rslt;
    		}		
    	}
    	return "";
    }
/*
    private String converToHashedIdentificationId(List<TelusIndividualIdentification> individualIdentification, String typ) {
    	if(individualIdentification==null || individualIdentification.isEmpty()){
    		return "";
    	}
    	for (TelusIndividualIdentification telusIndividualIdentification : individualIdentification) {	
    		if( typ.equals(telusIndividualIdentification.getIdentificationType()) ){		
    			String rslt = (telusIndividualIdentification.getIdentificationId()!=null)?telusIndividualIdentification.getIdentificationId().trim():"";
    			rslt = hashService.sha512CaseInsensitive(rslt);
    			return rslt;
    		}		
    	}
    	return "";
    }
 */
    private String getIdentificationIdHashed(List<TelusIndividualIdentification> individualIdentification, String typ) {
    	if(individualIdentification==null || individualIdentification.isEmpty()){
    		return "";
    	}
    	for (TelusIndividualIdentification telusIndividualIdentification : individualIdentification) {	
    		if( typ.equals(telusIndividualIdentification.getIdentificationType()) ){		
    			String rslt = (telusIndividualIdentification.getIdentificationIdHashed()!=null)?telusIndividualIdentification.getIdentificationIdHashed().trim():"";
    			return rslt;
    		}		
    	}
    	return "";
    }    
 	public TelusCreditProfile unmergeCreditprofiles(long tobeUnmergedCustomerId,TelusChannel aTelusChannel) { 

 		//get customer's Credit_profile_id
		Optional<CustomerCreditProfileRelEntity> customerCreditProfileRelOptinoal = customerCreditProfileRelDao.getCustomerCreditProfileRel_By_CustomerId_LOB(tobeUnmergedCustomerId,"WIRELINE");
		
		if(!customerCreditProfileRelOptinoal.isPresent()){
	        throw new CreditException(HttpStatus.BAD_REQUEST, 
	        		com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_1400
	        		, "Request validation failed", 
	        		com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_1400_MSG , 
	        		tobeUnmergedCustomerId+"");	        
		}
		
		CustomerCreditProfileRelEntity customerCreditProfileRel = customerCreditProfileRelOptinoal.get();
		String mergedCredit_profile_id= customerCreditProfileRel.getCreditProfileId();
		String mergedCreditProfileCustomerId= customerCreditProfileRel.getCreditProfileCustomerId();

		//get list of merged customers ( customers with same Credit_profile_id as input creditprofile) 
		Optional<List<CustomerCreditProfileRelEntity>> customerCreditProfileRelEntityList = customerCreditProfileRelDao.get_CustomerCreditProfileRel_ListByCreditProfileId(mergedCredit_profile_id);
		List<CustomerCreditProfileRelEntity> customerCreditProfileRelEntitys = customerCreditProfileRelEntityList.isPresent()? customerCreditProfileRelEntityList.get():new ArrayList<CustomerCreditProfileRelEntity>();
		if(customerCreditProfileRelEntitys.size() < 2){
			LOGGER.info("CustId={} customerCreditProfileRelEntitys.size()=", tobeUnmergedCustomerId, customerCreditProfileRelEntitys.size());
	        throw new CreditException(HttpStatus.BAD_REQUEST, 
	        		com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_1401
	        		, "Request validation failed", 
	        		com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_1401_MSG , 
	        		tobeUnmergedCustomerId+"");	        
		}
final Customer customerInDB;
TransactionTemplate txTemplate = new TransactionTemplate(txManager);
customerInDB = txTemplate.execute(transactionStatus -> 
{
	try {
		Customer tobeUnmergedCustomer = customerService.getCustomerPartyFromDatabase(new Long(tobeUnmergedCustomerId));
		
		//get WIRELINE Creditprofile
		List<TelusCreditProfile> dbCreditProfileList = tobeUnmergedCustomer.getCreditProfile();
    	List<TelusCreditProfile> filtereByLOB_DbCreditProfileListList = dbCreditProfileList.stream()
	            .filter(entity -> entity.getLineOfBusiness().equals("WIRELINE"))
	            .collect(Collectors.toList()); 
		
    	TelusCreditProfile tobeUnmergedCrediprofile = getPrimaryCreditProfile(filtereByLOB_DbCreditProfileListList);
    	//TelusCreditProfile tobeUnmergedCrediprofile = tobeUnmergedCustomer.getCreditProfile().get(0);
    	if(tobeUnmergedCrediprofile==null) {
    		return null;
    	}
		TelusCreditProfile defaultCreditProfile = createWirelineDefaultCreditProfile();
		//create creditprofile and customer-creditprofile mapping
        LOGGER.info("CustId={} Creating new default CreditProfile",tobeUnmergedCustomerId);
		String defaultCreditProfileId = creditProfileDao.insert(TelusCreditProfileModelMapper.toCreditProfileEntity(defaultCreditProfile)
                .createdBy(aTelusChannel.getUserId())
                .createdTs(new Timestamp(System.currentTimeMillis()))
                .updatedBy(aTelusChannel.getUserId())
                .updatedTs(new Timestamp(System.currentTimeMillis()))
                .originatorAppId(aTelusChannel.getOriginatorAppId())
                .channelOrgId(aTelusChannel.getChannelOrgId()));
		
		
		//customer-creditprofile mapping: Update customer’s entry in the mapping table with the new creditprofileId.
		LOGGER.info("CustId={} update customer-creditprofile mapping",tobeUnmergedCustomerId);
		 Optional<CustomerCreditProfileRelEntity> dbCustomerCreditProfileRelEntityOptional = customerCreditProfileRelDao.getCustomerCreditProfileRel_By_CreditProfileCustomerId(mergedCreditProfileCustomerId);
		 CustomerCreditProfileRelEntity dbCustomerCreditProfileRelEntity = dbCustomerCreditProfileRelEntityOptional.get();
		 dbCustomerCreditProfileRelEntity.setCreditProfileId(defaultCreditProfileId);
		 dbCustomerCreditProfileRelEntity.setUpdatedBy(aTelusChannel.getUserId());
		 dbCustomerCreditProfileRelEntity.setUpdatedOnTs(new Timestamp(System.currentTimeMillis()));
		 dbCustomerCreditProfileRelEntity.setOriginatorAppId(aTelusChannel.getOriginatorAppId());		
		 dbCustomerCreditProfileRelEntity.setChannelOrgId(aTelusChannel.getChannelOrgId());
		 customerCreditProfileRelDao.update(dbCustomerCreditProfileRelEntity.getCreditProfileCustomerId(), dbCustomerCreditProfileRelEntity);
		 
		//update merged customer's partyID to active customer's partyID
		 LOGGER.info("CustId={} update partyid in customer table ",tobeUnmergedCustomerId);
		 //create a empty party 
        String partyId = partyDao.insert(new PartyEntity()
                .partyType(PartyType.INDIVIDUAL.getType())
                .partyRole("Customer")
                .createdBy(aTelusChannel.getUserId())
                .updatedBy(aTelusChannel.getUserId())
                .originatorAppId(aTelusChannel.getOriginatorAppId())
                .channelOrgId(aTelusChannel.getChannelOrgId())
                );
        
		Optional<CustomerEntity> dbMergedCustomerEntityOptional = customerDao.findCustomerEntityByIdForUpdate(new Long(tobeUnmergedCustomerId) , "WIRELINE");
		CustomerEntity dbMergedCustomerEntity = dbMergedCustomerEntityOptional.get();
		dbMergedCustomerEntity.setPartyId(partyId);
		dbMergedCustomerEntity.setUpdatedBy(aTelusChannel.getUserId());
		dbMergedCustomerEntity.setCreatedTs(Timestamp.from(Instant.now()));
		customerDao.update(dbMergedCustomerEntity.getCreditProfileCustomerId(), dbMergedCustomerEntity);		 
		
		//create entry in crediprofile mapping history table (from crediprofile , to crediprofile) with type =UM(unmerged)
		CproflMappingEntity cprofileMappingEntity = new CproflMappingEntity();	
		cprofileMappingEntity.setCustomerId(new Long(tobeUnmergedCustomerId));
		cprofileMappingEntity.setCproflFromId(mergedCredit_profile_id);
		cprofileMappingEntity.setCproflToId(defaultCreditProfileId);
		cprofileMappingEntity.setCproflMappingTypCd("UM");
		cprofileMappingEntity.setCreatedBy(aTelusChannel.getUserId());
		cprofileMappingEntity.setCreatedOnTs(Timestamp.from(Instant.now()));
		cprofileMappingEntity.setUpdatedBy(aTelusChannel.getUserId());
		cprofileMappingEntity.setUpdatedOnTs(Timestamp.from(Instant.now()));
		cprofileMappingEntity.setOriginatorAppId(aTelusChannel.getOriginatorAppId());
		cprofileMappingEntity.setChannelOrgId(aTelusChannel.getChannelOrgId());
		cproflMappingDao.insert(cprofileMappingEntity);
		
		//TODO performOverrideCreditWorthiness  (Async)
		// will use Krishna's code when it is ready
		
		//Publish assessment message to pubusb to update assessment nosql  db AssessmentMsgCd=DFNOC01
        CreditAssessmentEvent event = createCreditAssessmentEvent("DFNOC01",tobeUnmergedCustomerId,defaultCreditProfileId,aTelusChannel);   
        try {       	
            messageSender.publish(event);
            LOGGER.info("Message published {}", event);
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            LOGGER.error("CustId={} , {}: {} Error publishing event[ {} ]. {}", tobeUnmergedCustomerId, ExceptionConstants.STACKDRIVER_METRIC, ExceptionConstants.PUBSUB200, event, ExceptionHelper.getStackTrace(e));           
            throw e;
        }

		//publish firestore sync message	
		Customer unmergedCustomer = customerService.getCustomerPartyFromDatabase(new Long(tobeUnmergedCustomerId));
		List<TelusCreditProfile> creditProfileList = unmergedCustomer.getCreditProfile();
		if(creditProfileList.size()>0 ) {
			List<Attachments> emptyAttchments = new ArrayList<Attachments>();
			emptyAttchments.add(new Attachments() );
			creditProfileList.get(0).setAttachments(emptyAttchments);		
		}		
		try {
		    long receivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
		    long submitterEventTime = DateTimeUtils.getRequestReceivedTimestampInMillis();			
			readDB.updateCustomerCollection(unmergedCustomer, null, "U", receivedTime,submitterEventTime,"UnMerge");
		} catch (PubSubPublishException e) {
			if (e instanceof PubSubPublishException) {
				LOGGER.error("CustId={}, {}: {} Error publishing sync message. {} ",tobeUnmergedCustomerId, ExceptionConstants.STACKDRIVER_METRIC,  ExceptionConstants.PUBSUB201, ExceptionHelper.getStackTrace(e));
			} else {
				LOGGER.error("CustId={} {} Error publishing sync message. {} ", tobeUnmergedCustomerId, ExceptionConstants.STACKDRIVER_METRIC, ExceptionHelper.getStackTrace(e));
			}
			throw e;
		}	 
		
		//STEP:publish change audit doc to be stored in firestore
		CreditProfileAuditDocument auditDocument = AuditService.auditContext();
		//auditDocument.setCorrelationId(requestContext.getCorrId());
		auditDocument.setCustomerId(Objects.toString(tobeUnmergedCustomerId));
		auditDocument.setChannelOrgId(aTelusChannel.getChannelOrgId());
		auditDocument.setOriginatingAppId(aTelusChannel.getOriginatorAppId());
		auditDocument.setUserId(aTelusChannel.getUserId());		
		auditDocument.setEventTimestamp(Timestamp.from(Instant.now()));
		auditDocument.setEventType(CreditProfileAuditDocument.EventType.CP_UPDATE);		
		responseInterceptorService.resolveMissingFieldsAndAudit(tobeUnmergedCrediprofile,unmergedCustomer);
		
		//STEP: publish the incomingCreditProfile to world	
		responseInterceptorService.decryptCustomerFromDb(unmergedCustomer);			    
		creditProfileEventSender.publish(Collections.singletonList(unmergedCustomer.getCreditProfile().get(0)),"CreditProfileCreateEvent");		
		

		//return unmergedCustomer.getCreditProfile().get(0);
		LOGGER.info("end of unmergeCreditprofiles ");
		return unmergedCustomer;
	}catch (Exception e) {
		LOGGER.error("CustId={} {} update transaction failed . {} ", tobeUnmergedCustomerId, ExceptionConstants.STACKDRIVER_METRIC, ExceptionHelper.getStackTrace(e));
		transactionStatus.setRollbackOnly();
	}
	return null;	
});
	
	TelusCreditProfile unmergedCreditProfile = (customerInDB.getCreditProfile()!=null && !customerInDB.getCreditProfile().isEmpty())?customerInDB.getCreditProfile().get(0):null;
	 return  unmergedCreditProfile;
}
	
	
	
	/*
	 * -AssessmentMsgCd=DFNOC01
	 */
	private CreditAssessmentEvent createCreditAssessmentEvent(String defaultAssessmentMessageCd, long mergedCustomerId ,String defaultCreditProfileId,  TelusChannel aTelusChannel) {
		String nowDt = Instant.now().toString();
       CreditAssessmentEvent event = new CreditAssessmentEvent();
       event.setDescription("assessment Unmerge");         
       event.setEventId(UUID.randomUUID().toString());
       event.setEventType("assessmentCreate");
       //event.setEventTime(nowDt);
            
			
	        List<CreditAssessment> creditAssessmentList=new  ArrayList<CreditAssessment>();
	        CreditAssessment creditAssessment = new CreditAssessment();
	        creditAssessment.setAssessmentMessageCd(defaultAssessmentMessageCd);
	        creditAssessment.setCustomerId(mergedCustomerId+"");
	        creditAssessment.setId(mergedCustomerId+"");
	        creditAssessment.setCreditProfileId(defaultCreditProfileId);
	/*        
	        creditAssessment.setCreditAssessmentId("");
	        creditAssessment.setCreditAssessmentResultCd("");
	        creditAssessment.setCreditAssessmentResultReasonCd("");
	        creditAssessment.setCreditAssessmentSubTypeCd("");
	        creditAssessment.setCreditAssessmentTimestamp(nowDt);
	        creditAssessment.setCreditAssessmentTypeCd("");
	 */       
	
	        creditAssessment.setChannelOrgId(aTelusChannel.getChannelOrgId());
	        creditAssessment.setCreatedBy(aTelusChannel.getUserId());
	        creditAssessment.setUpdatedBy(aTelusChannel.getUserId());
	        creditAssessment.setOriginatorAppId(aTelusChannel.getOriginatorAppId());
	         creditAssessment.setCreatedTimestamp(nowDt);
	        creditAssessment.setUpdatedTimestamp(nowDt);
	        
	        creditAssessmentList.add(creditAssessment);
	    event.setEvent(creditAssessmentList);       
		return event;
	
	}

	/*
	 * create default creditprofile with and no party data( no individual,address,identifications,..)
	 * -Credit value = N
	 * -AssessmentMsgCd=DFNOC01
	 * -DecisionCd=NOCONSENT001
	 * -RiskLevelNum=7000
	 * -Product Set Qualifications SLR
	 * -BoltOn =false
	 * 	-Comment ="Default - Credit Assessment not complete"
	 */
	private TelusCreditProfile createWirelineDefaultCreditProfile() {
		String nowDt = DateTimeUtils.toUtcString(new Date());
		TelusCreditProfile telusCreditProfile = new TelusCreditProfile();
		telusCreditProfile.setCreationTs(nowDt);
		telusCreditProfile.setCreditClassCd("N");
		telusCreditProfile.setCreditClassDate(nowDt);
		telusCreditProfile.setCreditDecisionCd("NOCONSENT001");
		telusCreditProfile.setCreditDecisionDate(nowDt);
		telusCreditProfile.setCreditRiskLevelNum("7000");
		telusCreditProfile.setRiskLevelDt(nowDt);
		telusCreditProfile.setLineOfBusiness("WIRELINE");		
		telusCreditProfile.setStatusCd("A");
		telusCreditProfile.setStatusTs(nowDt);
		
		List<TelusCharacteristic> characteristicList = new ArrayList<TelusCharacteristic>();
		TelusCharacteristic commentCharacteristic = new TelusCharacteristic();
		commentCharacteristic.setName(CreditProfileConstant.COMMENT_TXT);
		commentCharacteristic.setValue("Default - Credit Assessment not complete");
		commentCharacteristic.setValueType("String");		
		characteristicList.add(commentCharacteristic);		
		
		TelusCharacteristic buslastupdatetsCharacteristic = new TelusCharacteristic();
		buslastupdatetsCharacteristic.setName(CreditProfileConstant.BUS_LAST_UPDT_TS);
		buslastupdatetsCharacteristic.setValue(nowDt);
		buslastupdatetsCharacteristic.setValueType("String");		
		characteristicList.add(buslastupdatetsCharacteristic);			
		telusCreditProfile.setCharacteristic(characteristicList);
		
		TimePeriod timePeriod = new TimePeriod();
		timePeriod.setStartDateTime(nowDt);
		telusCreditProfile.setValidFor(timePeriod);

		return telusCreditProfile;
	}

}
