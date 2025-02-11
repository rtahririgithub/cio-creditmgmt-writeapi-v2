package com.telus.credit.service.impl;

import com.google.common.collect.Lists;
import com.telus.credit.common.CommonHelper;
import com.telus.credit.common.CreditProfileConstant;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.common.ErrorCode;
import com.telus.credit.dao.IdentificationAttributesDao;
import com.telus.credit.dao.IndividualDao;
import com.telus.credit.dao.OrganizationDao;
import com.telus.credit.dao.PartyContactMediumDao;
import com.telus.credit.dao.PartyDao;
import com.telus.credit.dao.entity.IndividualEntity;
import com.telus.credit.dao.entity.OrganizationEntity;
import com.telus.credit.dao.entity.PartyContactMediumEntity;
import com.telus.credit.dao.entity.PartyEntity;
import com.telus.credit.dao.entity.PartyIdentificationExEntity;
import com.telus.credit.exceptions.CreditException;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.model.ContactMedium;
import com.telus.credit.model.Individual;
import com.telus.credit.model.Organization;
import com.telus.credit.model.OrganizationIdentification;
import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.RelatedPartyInterface;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCharacteristic;
import com.telus.credit.model.TelusIndividualIdentification;
import com.telus.credit.model.common.PartyType;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.model.mapper.ContactMediumModelMapper;
import com.telus.credit.model.mapper.IndividualModelMapper;
import com.telus.credit.model.mapper.TelusIndividualIdentificationModelMapper;
import com.telus.credit.model.mapper.TelusOrganizationIdentificationModelMapper;
import com.telus.credit.pubsub.service.TelusMDMEventSender;
import com.telus.credit.service.EngagedPartyService;
import com.telus.credit.service.ValidationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_8000;
import static com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_8000_MSG;
import static com.telus.credit.util.Utils.getCharacteristicValue;

@Service
public class DefaultEngagedPartyService implements EngagedPartyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEngagedPartyService.class);

    public static final String CUSTOMER = "Customer";

    private final PartyDao partyDao;

    private final IndividualDao individualDao;

    private final OrganizationDao organizationDao;

    private final PartyContactMediumDao partyContactMediumDao;

    private final IdentificationAttributesDao identificationAttributesDao;

    private final PartyIdentificationService partyIdentificationService;

    private final ValidationService validationService;

    private final TelusMDMEventSender mdmEventSender;

    public DefaultEngagedPartyService(PartyDao partyDao,
                                      PartyContactMediumDao partyContactMediumDao,
                                      PartyIdentificationService partyIdentificationService,
                                      IdentificationAttributesDao identificationAttributesDao,
                                      IndividualDao individualDao,
                                      OrganizationDao organizationDao,
                                      ValidationService validationService,
                                      TelusMDMEventSender mdmEventSender) {
        this.partyDao = partyDao;
        this.individualDao = individualDao;
        this.organizationDao = organizationDao;
        this.partyContactMediumDao = partyContactMediumDao;
        this.partyIdentificationService = partyIdentificationService;
        this.identificationAttributesDao = identificationAttributesDao;
        this.validationService = validationService;
        this.mdmEventSender = mdmEventSender;
    }

    /**
     * Determine if a party is Individual or Organization
     *
     * @param engagedParty
     * @return Individual or Organization
     */
    private String getReferredType(RelatedPartyToPatch engagedParty) {
        String type = engagedParty.getIndividualIdentification() != null ? PartyType.INDIVIDUAL.getType() : PartyType.ORGANIZATION.getType();
        type = StringUtils.isBlank(engagedParty.getAtReferredType()) ? type : engagedParty.getAtReferredType();
        type = StringUtils.capitalize(type);
        return type;
    }

    /**
     * Create engaged party and its attributes. There is no update in this flow
     *
     * @param engagedParty
     * @param auditCharacteristic
     * @return party UID
     */
    @Override
    public String createEngagedParty(RelatedPartyToPatch engagedParty, TelusChannel auditCharacteristic) {
        if (engagedParty == null) {
            return null;
        }

        validationService.validateForCreate(engagedParty);

        String type = getReferredType(engagedParty);

        String partyId = partyDao.insert(new PartyEntity()
                .partyType(type)
                .partyRole(CUSTOMER)
                .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                .originatorAppId(auditCharacteristic.getOriginatorAppId())
                .channelOrgId(auditCharacteristic.getChannelOrgId()));
        engagedParty.setId(partyId);
        engagedParty.setRole(CUSTOMER);

        List<String> encryptedAttrs = this.identificationAttributesDao.selectAll().stream()
                .filter(e -> e.getIsEncrypted()).map(e -> e.getAttributeName())
                .collect(Collectors.toList());

        if (PartyType.INDIVIDUAL.getType().equalsIgnoreCase(type)) {
            LOGGER.info("Creating individual party {}", partyId);
            createIndividual(partyId, engagedParty, auditCharacteristic, encryptedAttrs);
        } else {
            LOGGER.info("Creating organization party {}", partyId);
            createOrganization(partyId, engagedParty, auditCharacteristic, encryptedAttrs);
        }

        saveContactMedium(partyId, engagedParty.getContactMedium(), auditCharacteristic);

       
        return partyId;
    }

    private void createIndividual(String partyId, RelatedPartyToPatch engagedParty,
                                  TelusChannel auditCharacteristic, List<String> encryptedAttrs) {

        individualDao.insert(IndividualModelMapper.toEntity(partyId, engagedParty, auditCharacteristic));

        for (TelusIndividualIdentification identification : CommonHelper.nullSafe(engagedParty.getIndividualIdentification())) {
            PartyIdentificationExEntity partyIdentificationEntity =
                    TelusIndividualIdentificationModelMapper.toEntity(identification, encryptedAttrs);

            partyIdentificationService.createIdentification(partyId, partyIdentificationEntity, auditCharacteristic);
        }
    }

    private void createOrganization(String partyId, RelatedPartyToPatch engagedParty,
    		TelusChannel auditCharacteristic, List<String> encryptedAttrs) {
        organizationDao.insert(new OrganizationEntity()
                .partyId(partyId)
                .birthDate(DateTimeUtils.toUtcDate(engagedParty.getBirthDate()))
                .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                .originatorAppId(auditCharacteristic.getOriginatorAppId())
                .channelOrgId(auditCharacteristic.getChannelOrgId()));

        for (OrganizationIdentification identification : CommonHelper.nullSafe(engagedParty.getOrganizationIdentification())) {
            PartyIdentificationExEntity partyIdentificationEntity =
                    TelusOrganizationIdentificationModelMapper.toEntity(identification, encryptedAttrs);
            partyIdentificationService.createIdentification(partyId, partyIdentificationEntity, auditCharacteristic);
        }
    }

    /**
     * Update engaged party attributes. Update if contactMediums exists (ID != null), add new if they doesn't
     *
     * @param partyId
     * @param relatedParty
     * @param auditCharacteristic
     */
    @Override
    public void patchEngagedParty(String partyId, RelatedParty relatedParty, TelusChannel auditCharacteristic, String consentCode, String lineOfBusiness) {

        if (StringUtils.isBlank(partyId) || Objects.isNull(relatedParty) || Objects.isNull(relatedParty.getEngagedParty())) {
            return;
        }
        RelatedPartyToPatch engagedParty = relatedParty.getEngagedParty();
        validationService.validateForPatch(engagedParty);

        String currentType = partyDao.getById(partyId).map(PartyEntity::getPartyType)
                .orElseThrow(() -> new CreditException(HttpStatus.BAD_REQUEST, ERR_CODE_8000, ERR_CODE_8000_MSG, "Party not found"));

        boolean isWireline = "WIRELINE".equals(lineOfBusiness);
        
        if (PartyType.INDIVIDUAL.getType().equalsIgnoreCase(currentType)) {
            LOGGER.info("Updating individual party {}", partyId);
            boolean publishMDM=isWireline;
            patchIndividual(partyId, relatedParty, auditCharacteristic, publishMDM);
        } else {
            LOGGER.info("Updating organization party {}", partyId);
            patchOrganization(partyId, engagedParty, auditCharacteristic);
        }
       
        if ("N".equals(consentCode) && isWireline) {
            deleteContactMedium(partyId, engagedParty.getContactMedium(), auditCharacteristic);
        } else {
            saveContactMedium(partyId, engagedParty.getContactMedium(), auditCharacteristic);
        }

    }

    private void deleteContactMedium(String partyId, List<ContactMedium> contactMedium, TelusChannel auditCharacteristic) {
        partyContactMediumDao.removeByPartyIds(Lists.newArrayList(partyId));
    }

    /**
     * Create contact mediums if they doesn't exist (ID is null). Update if they does.
     *
     * @param partyId
     * @param contactMedium
     * @param auditCharacteristic
     */
    private void saveContactMedium(String partyId, List<ContactMedium> contactMedium, TelusChannel auditCharacteristic) {
        for (ContactMedium contact : CommonHelper.nullSafe(contactMedium)) {
            PartyContactMediumEntity entity = ContactMediumModelMapper.toEntity(contact);

            //direct call to API to create new contact
            if (StringUtils.isBlank(contact.getId()) && !auditCharacteristic.getTenpubsubsync()) {
                validationService.validateForCreate(contact);
                LOGGER.info("Adding contact medium for party {}", partyId);
                createContactMedium(partyId, auditCharacteristic, contact, entity);                
            } else {
            	    //direct call to API to update new contact
	            	if (!auditCharacteristic.getTenpubsubsync()) {            	
						updateContactMedium(partyId, auditCharacteristic, contact, entity);
		            } 
	            	//async call to API to update new contact
	            	else {
		                savePubSubContactMedium(partyId, entity, auditCharacteristic);
		            }
            }
        }
    }

	private void createContactMedium(String partyId, TelusChannel auditCharacteristic, ContactMedium contact,
			PartyContactMediumEntity entity) {
		String contactId = partyContactMediumDao.insert(entity
		        .partyId(partyId)
		        .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
		        .originatorAppId(auditCharacteristic.getOriginatorAppId())
		        .channelOrgId(auditCharacteristic.getChannelOrgId()));
		contact.setId(contactId);
	}

	private void updateContactMedium(String partyId, TelusChannel auditCharacteristic, ContactMedium contact,
			PartyContactMediumEntity entity) {
		Optional<PartyContactMediumEntity> existingEntity = partyContactMediumDao.getById(contact.getId());
		if (!existingEntity.isPresent() || !partyId.equals(existingEntity.get().getPartyId())) {
            LOGGER.error("{} , partyId={}: {}, {} , {}", ExceptionConstants.STACKDRIVER_METRIC, partyId,ErrorCode.C_1121.code(), ErrorCode.C_1121.getMessage(),  "Contact medium " + contact.getId() + " was not found or doesn't belong to this party");
			return;
			//throw new CreditException(HttpStatus.BAD_REQUEST, ErrorCode.C_1121.code(), ErrorCode.C_1121.getMessage(),"Contact medium " + contact.getId() + " was not found or doesn't belong to this party");
		}

		LOGGER.info("Updating contact medium for party {}", partyId);
		int updateCount = partyContactMediumDao.update(contact.getId(), entity
		        .version(existingEntity.get().getVersion())
		        .updatedBy(auditCharacteristic.getUserId())
		        .originatorAppId(auditCharacteristic.getOriginatorAppId())
		        .channelOrgId(auditCharacteristic.getChannelOrgId()));
		if (updateCount > 1) {
		    //Allow transaction to complete. if there are more than on entries in DB with the same ID , all shall be updated
			String msg="";
			msg= "[" ;
			msg = msg  + "ActualSize=" + updateCount;
			msg = msg  + ",ExpectedSize=" +1;
			msg = msg  + ",Message=partyContactMediumDao.update operation updated multiple rows for same partyId=" +partyId ;
			msg = msg  + "]";  
			LOGGER.warn("{} Data Access Exception. {} ", ExceptionConstants.POSTGRES100,msg);                     
		}else {
			if (updateCount !=1) {
		         throw new IncorrectResultSizeDataAccessException("partyId=" +partyId + " partyContactMediumDao.update",1,updateCount);
			}
		}
	}

    /**
     * Apply for ten pubsub or capi. Get the most recently updated contact and update the attributes
     *
     * @param partyId
     * @param entity
     * @param auditCharacteristic
     */
    private void savePubSubContactMedium(String partyId, PartyContactMediumEntity entity, TelusChannel auditCharacteristic) {
        List<PartyContactMediumEntity> contacts = partyContactMediumDao.getByPartyIds(Collections.singletonList(partyId));
        contacts.sort(Comparator.comparing(PartyContactMediumEntity::getUpdatedTs));
        if (contacts.isEmpty()) {
            partyContactMediumDao.insert(entity
                    .partyId(partyId)
                    .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                    .originatorAppId(auditCharacteristic.getOriginatorAppId())
                    .channelOrgId(auditCharacteristic.getChannelOrgId()));
        } else {
            PartyContactMediumEntity existingEntity = CollectionUtils.lastElement(contacts);
            int updateCount = partyContactMediumDao.update(existingEntity.getContactMediumId(), entity
                    .version(existingEntity.getVersion())
                    .updatedBy(auditCharacteristic.getUserId())
                    .originatorAppId(auditCharacteristic.getOriginatorAppId())
                    .channelOrgId(auditCharacteristic.getChannelOrgId()));
            if (updateCount > 1) {
                //Allow transaction to complete. if there are more than on entries in DB with the same ID , all shall be updated.            	
            	String msg="";
    	    	msg= "[" ;
    	    	msg = msg  + "ActualSize=" + updateCount;
    	    	msg = msg  + ",ExpectedSize=" +1;
    	    	msg = msg  + ",Message=partyContactMediumDao.update operation updated multiple rows for same partyId=" +partyId ;
    	    	msg = msg  + "]";  
    	    	LOGGER.warn("{} Data Access Exception. {} ", ExceptionConstants.POSTGRES100,msg);                  
            } else {
               if (updateCount != 1) {
                  throw new IncorrectResultSizeDataAccessException("partyId=" +partyId + " partyContactMediumDao.update",1,updateCount);   
               }
            }
        }
    }

    /**
     * Update existing Individual
     *
     * @param partyId uid string
     * @param relatedParty
     * @param auditCharacteristic
     */
    private void patchIndividual(String partyId, RelatedParty relatedParty, TelusChannel auditCharacteristic, boolean publishMDM) {
        RelatedPartyToPatch engagedParty = relatedParty.getEngagedParty();
        List<String> encryptedAttrs = this.identificationAttributesDao.selectAll().stream()
                .filter(e -> e.getIsEncrypted()).map(e -> e.getAttributeName())
                .collect(Collectors.toList());

        List<IndividualEntity> byPartyIds = individualDao.getByPartyIds(Collections.singletonList(partyId));
        if (CollectionUtils.isEmpty(byPartyIds)) {
            throw new CreditException(HttpStatus.INTERNAL_SERVER_ERROR, ERR_CODE_8000, ERR_CODE_8000_MSG, "Party not found.partyId=" + partyId + " ");
        }

        IndividualEntity existingEntity = byPartyIds.get(0);
        IndividualEntity individualEntity = new IndividualEntity()
                .birthDate(DateTimeUtils.toUtcDate(engagedParty.getBirthDate()), engagedParty.getBirthDatePatch() != null)
                .version(existingEntity.getVersion()).updatedBy(auditCharacteristic.getUserId())
                .originatorAppId(auditCharacteristic.getOriginatorAppId())
                .channelOrgId(auditCharacteristic.getChannelOrgId());
        PatchField<List<TelusCharacteristic>> characteristicsPatch = engagedParty.getCharacteristicsPatch();
        if (characteristicsPatch != null && !characteristicsPatch.isValueNull()) {
            List<TelusCharacteristic> characteristicList = characteristicsPatch.get();
            individualEntity.setEmploymentStatusCd(getCharacteristicValue(characteristicList, CreditProfileConstant.EMPLOYMENT_STATUS_CD));
            individualEntity.setLegalCareCd(getCharacteristicValue(characteristicList, CreditProfileConstant.LEGAL_CARE_CD));
            individualEntity.setPrimCredCardTypCd(getCharacteristicValue(characteristicList, CreditProfileConstant.PRIM_CRED_CARD_TYP_CD));
            individualEntity.setResidencyCd(getCharacteristicValue(characteristicList, CreditProfileConstant.RESIDENCY_CD));
            individualEntity.setSecCredCardIssCoTypCd(getCharacteristicValue(characteristicList, CreditProfileConstant.SEC_CRED_CARD_ISS_CO_TYP_CD));
        }
        int count = individualDao.update(partyId, individualEntity);

        if (count != 1) {
            //Allow transaction to complete. if there are more than on entries in DB with the same ID , all shall be updated.            	
            String msg = "";
            msg = "[";
            msg = msg + "ActualSize=" + count;
            msg = msg + ",ExpectedSize=" + 1;
            msg = msg + ",Message=individualDao.update operation updated multiple rows for same partyId=" + partyId;
            msg = msg + "]";
            LOGGER.warn("{} Data Access Exception. {} ", ExceptionConstants.POSTGRES100, msg);
        } else {
            if (count != 1) {
                throw new IncorrectResultSizeDataAccessException("partyId=" + partyId + " individualDao.update", 1, count);
            }
        }

        for (TelusIndividualIdentification identification : CommonHelper.nullSafe(engagedParty.getIndividualIdentification())) {
            partyIdentificationService.patchIndividualIdentification(partyId, identification, auditCharacteristic, encryptedAttrs, publishMDM);
        }
        //publish  credit id update to MDM
        if (publishMDM) {
            String messageCardIds = mdmEventSender.createJSONMessageForCreditIds(engagedParty.getIndividualIdentification(), relatedParty.getId(), auditCharacteristic.getUserId(), "UPDATE").toString();
            mdmEventSender.publish(messageCardIds);
            //Publish date of birth change to MDM
            if (Objects.nonNull(engagedParty.getBirthDatePatch()) && !engagedParty.getBirthDatePatch().isValueNull()) {
                Date dateOfBirth = DateTimeUtils.toUtcDate(engagedParty.getBirthDatePatch().get());
                String message = mdmEventSender.createJSONMessageForDOB(relatedParty.getId(), dateOfBirth, auditCharacteristic.getUserId()).toString();
                mdmEventSender.publish(message);
            }
        }

    }

    /**
     * Update existing Organization
     *
     * @param partyId uid string
     * @param engagedParty
     * @param auditCharacteristic
     */
    private void patchOrganization(String partyId, RelatedPartyToPatch engagedParty,
    		TelusChannel auditCharacteristic) {
        List<String> encryptedAttrs = this.identificationAttributesDao.selectAll().stream()
                .filter(e -> e.getIsEncrypted()).map(e -> e.getAttributeName())
                .collect(Collectors.toList());

        List<OrganizationEntity> byPartyIds = organizationDao.getByPartyIds(Collections.singletonList(partyId));
        if (CollectionUtils.isEmpty(byPartyIds)) {
            throw new CreditException(HttpStatus.INTERNAL_SERVER_ERROR, ERR_CODE_8000, ERR_CODE_8000_MSG, "Party not found.partyId="+ partyId + " ");
        }

        OrganizationEntity existingEntity = byPartyIds.get(0);
        int count = organizationDao.update(partyId, new OrganizationEntity()
                .birthDate(DateTimeUtils.toUtcDate(engagedParty.getBirthDate()), engagedParty.getBirthDatePatch() != null)
                .version(existingEntity.getVersion()).updatedBy(auditCharacteristic.getUserId())
                .originatorAppId(auditCharacteristic.getOriginatorAppId())
                .channelOrgId(auditCharacteristic.getChannelOrgId()));
        if (count != 1) {
            //Allow transaction to complete. if there are more than on entries in DB with the same ID , all shall be updated.            	
        	String msg="";
	    	msg= "[" ;
	    	msg = msg  + "ActualSize=" + count;
	    	msg = msg  + ",ExpectedSize=" +1;
	    	msg = msg  + ",Message=individualDao.update operation updated multiple rows for same partyId=" +partyId ;
	    	msg = msg  + "]";  
	    	LOGGER.warn("{} Data Access Exception. {} ", ExceptionConstants.POSTGRES100,msg);             
        } else {
           if (count != 1) {
              throw new IncorrectResultSizeDataAccessException("partyId=" +partyId + " organizationDao.update",1,count);  
           }
        }        
        
        
        for (OrganizationIdentification identification : CommonHelper.nullSafe(engagedParty.getOrganizationIdentification())) {
            partyIdentificationService.patchOrganizationIdentification(partyId, identification, auditCharacteristic, encryptedAttrs);
        }
    }

    /**
     * Get engaged Party by UID
     *
     * @param partyId party UID
     * @return Individual or Organization DTO
     */
    @Override
    public RelatedPartyInterface getEngagedParty(String partyId) {
        Optional<PartyEntity> result = partyDao.getById(partyId);
        if (!result.isPresent()) {
            return null;
        }
        PartyEntity partyEntity = result.get();

        List<PartyContactMediumEntity> contactMediumEntities = partyContactMediumDao.getByPartyIds(Collections.singletonList(partyId));

        List<PartyIdentificationExEntity> identificationEntities = partyIdentificationService.getIdentificationEntities(partyId);

        if (PartyType.INDIVIDUAL.getType().equalsIgnoreCase(partyEntity.getPartyType())) {
            List<IndividualEntity> individualEntities = individualDao.getByPartyIds(Collections.singletonList(partyId));
            Individual individual = TelusIndividualIdentificationModelMapper.toDto(partyEntity,individualEntities.isEmpty() ? null : individualEntities.get(0), identificationEntities);
            contactMediumEntities.forEach(e -> individual.getContactMedium().add(ContactMediumModelMapper.toDto(e)));
            return individual;
        }else {
        	if (PartyType.ORGANIZATION.getType().equalsIgnoreCase(partyEntity.getPartyType())) {
        		List<OrganizationEntity> organizationEntities = organizationDao.getByPartyIds(Collections.singletonList(partyId));
		        Organization organization = TelusOrganizationIdentificationModelMapper.toDto(partyEntity,organizationEntities.isEmpty() ? null : organizationEntities.get(0), identificationEntities);

		        contactMediumEntities.forEach(e -> organization.getContactMedium().add(ContactMediumModelMapper.toDto(e)));
		        return organization;
        	}
        }
		return null;
    }
}
