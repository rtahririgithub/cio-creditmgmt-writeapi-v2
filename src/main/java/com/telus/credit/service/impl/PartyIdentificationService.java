package com.telus.credit.service.impl;

import com.telus.credit.common.CommonHelper;
import com.telus.credit.dao.IdentificationCharDao;
import com.telus.credit.dao.IdentificationCharHashDao;
import com.telus.credit.dao.PartyIdentificationDao;
import com.telus.credit.dao.entity.IdentificationCharEntity;
import com.telus.credit.dao.entity.IdentificationCharHashEntity;
import com.telus.credit.dao.entity.PartyIdentificationExEntity;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.model.OrganizationIdentification;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusIndividualIdentification;
import com.telus.credit.model.mapper.TelusIndividualIdentificationModelMapper;
import com.telus.credit.model.mapper.TelusOrganizationIdentificationModelMapper;
import com.telus.credit.pubsub.service.TelusMDMEventSender;
import com.telus.credit.service.ValidationService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PartyIdentificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyIdentificationService.class);

    private final PartyIdentificationDao partyIdentificationDao;

    private final IdentificationCharDao identificationCharDao;

    private final IdentificationCharHashDao identificationCharHashDao;

    private final ValidationService validationService;
    private final TelusMDMEventSender mdmEventSender;

    public PartyIdentificationService(PartyIdentificationDao partyIdentificationDao,
                                      IdentificationCharDao identificationCharDao,
                                      IdentificationCharHashDao identificationCharHashDao,
                                      ValidationService validationService,
                                      TelusMDMEventSender mdmEventSender) {
        this.partyIdentificationDao = partyIdentificationDao;
        this.identificationCharDao = identificationCharDao;
        this.identificationCharHashDao = identificationCharHashDao;
        this.validationService = validationService;
        this.mdmEventSender = mdmEventSender ;
    }

    public void removeAll(String partyId) {
        List<PartyIdentificationExEntity> identificationEntities = getIdentificationEntities(partyId);
        identificationEntities.forEach(id -> {
            LOGGER.debug("identifications attr hash removed: {}, partyId={}",
                    identificationCharHashDao.deleteDeleteByIdentificationId(id.getIdentificatonId()), id);
            LOGGER.debug("identifications attr removed: {}, partyId={}",
                    identificationCharDao.deleteDeleteByIdentificationId(id.getIdentificatonId()), id);
        });

        LOGGER.debug("Identification removed: {}, partyId={}",
                partyIdentificationDao.removeByPartyIds(Collections.singletonList(partyId)), partyId);
    }

    /**
     * Get Identification entities and its characteristic (hash)
     * for a party (Individual or Organization)
     *
     * @param partyId party UID
     * @return
     */
    public List<PartyIdentificationExEntity> getIdentificationEntities(String partyId) {
        if (StringUtils.isBlank(partyId)) {
            return Collections.emptyList();
        }

        List<PartyIdentificationExEntity> partyIdentificationEntities = partyIdentificationDao.getByPartyIds(Collections.singletonList(partyId));
        Map<String, PartyIdentificationExEntity> identificationExEntityMap = new HashMap<>();
        partyIdentificationEntities.forEach(e -> identificationExEntityMap.put(e.getIdentificatonId(), e));

        identificationCharDao.getByIdentificationIds(identificationExEntityMap.keySet())
                .forEach(e -> identificationExEntityMap.get(e.getIdentificatonId()).getCharacteristic().add(e));
        identificationCharHashDao.getByIdentificationIds(identificationExEntityMap.keySet())
                .forEach(e -> identificationExEntityMap.get(e.getIdentificatonId()).getHashedCharacteristics().add(e));

        return partyIdentificationEntities;
    }

    /**
     * Create PartyIdentification and its characteristic (Hash)
     * for a party (Individual or Organization)
     *
     * @param partyId party UID
     * @param partyIdentificationEntity PartyIdentification and its characteristic (hashed)
     * @param auditCharacteristic
     */
    public void createIdentification(String partyId, PartyIdentificationExEntity partyIdentificationEntity,
    		TelusChannel auditCharacteristic) {
        String partyIdentificationId = partyIdentificationDao.insert(partyIdentificationEntity
                .partyId(partyId)
                .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                .originatorAppId(auditCharacteristic.getOriginatorAppId())
                .channelOrgId(auditCharacteristic.getChannelOrgId()));


        for (IdentificationCharEntity characteristicEntity : CommonHelper.nullSafe(partyIdentificationEntity.getCharacteristic())) {
            if (!StringUtils.isBlank(characteristicEntity.getValue())) {
                identificationCharDao.insert(characteristicEntity
                        .identificatonId(partyIdentificationId)
                        .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                        .originatorAppId(auditCharacteristic.getOriginatorAppId())
                        .channelOrgId(auditCharacteristic.getChannelOrgId()));
            }
        }
        for (IdentificationCharHashEntity characteristicHashEntity : CommonHelper.nullSafe(partyIdentificationEntity.getHashedCharacteristics())) {
            if (!StringUtils.isBlank(characteristicHashEntity.getValue())) {
                identificationCharHashDao.insert(characteristicHashEntity
                        .identificatonId(partyIdentificationId)
                        .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                        .originatorAppId(auditCharacteristic.getOriginatorAppId())
                        .channelOrgId(auditCharacteristic.getChannelOrgId()));
            }
        }
    }

    /**
     * Map and create Organization identification characteristic (hash)
     * if it doesn't exists or update if it does
     *
     * @param partyId party UID
     * @param identification
     * @param auditCharacteristic
     * @param encryptedAttrs
     */
    public void patchOrganizationIdentification(String partyId, OrganizationIdentification identification,
    		TelusChannel auditCharacteristic, List<String> encryptedAttrs) {
        PartyIdentificationExEntity partyIdentificationEntity =
                TelusOrganizationIdentificationModelMapper.toEntity(identification, encryptedAttrs);

        Optional<PartyIdentificationExEntity> existingPartyIdEntity = partyIdentificationDao.getByPartyIdAndIdType(partyId, partyIdentificationEntity.getIdType());
        if (!existingPartyIdEntity.isPresent()) {
            validationService.validateForCreate(identification);
            createIdentification(partyId, partyIdentificationEntity, auditCharacteristic);
            return;
        }

        String identificationUid = existingPartyIdEntity.get().getIdentificatonId();

        patchCharacteristics(existingPartyIdEntity.get(), partyIdentificationEntity, auditCharacteristic);
        patchCharacteristicHash(identificationUid, partyIdentificationEntity, auditCharacteristic);
    }
    /**
     * Map and create Individual identification characteristic (hash)
     * if it doesn't exists or update if it does
     *
     * @param partyId
     * @param identification
     * @param auditCharacteristic
     * @param encryptedAttrs
     */
    public void patchIndividualIdentification(String partyId, TelusIndividualIdentification identification,
                                              TelusChannel auditCharacteristic, List<String> encryptedAttrs) throws JSONException {
        patchIndividualIdentification(partyId, identification, auditCharacteristic, encryptedAttrs,false );
    }
    /**
     * Map and create Individual identification characteristic (hash)
     * if it doesn't exists or update if it does
     *
     * @param partyId
     * @param identification
     * @param auditCharacteristic
     * @param encryptedAttrs
     */
    public void patchIndividualIdentification(String partyId, TelusIndividualIdentification identification,
    		TelusChannel auditCharacteristic, List<String> encryptedAttrs,boolean publishMDM) {

        PartyIdentificationExEntity partyIdentificationEntity =
                TelusIndividualIdentificationModelMapper.toEntity(identification, encryptedAttrs);

        Optional<PartyIdentificationExEntity> existingPartyIdEntity = partyIdentificationDao.getByPartyIdAndIdType(partyId, partyIdentificationEntity.getIdType());
        if (!existingPartyIdEntity.isPresent()) {
            validationService.validateForCreate(identification);
            createIdentification(partyId, partyIdentificationEntity, auditCharacteristic);
            return;
        }

        String identificationUid = existingPartyIdEntity.get().getIdentificatonId();

        patchCharacteristics(existingPartyIdEntity.get(), partyIdentificationEntity, auditCharacteristic, publishMDM);
        patchCharacteristicHash(identificationUid, partyIdentificationEntity, auditCharacteristic);

    }

    /**
     * Update a PartyIdentification and its characteristic.
     * If its characteristic don't exists, create otherwise update
     *
     * @param existingPartyIdEntity
     * @param partyIdentificationEntity
     * @param auditCharacteristic
     */
    private void patchCharacteristics(PartyIdentificationExEntity existingPartyIdEntity, PartyIdentificationExEntity partyIdentificationEntity, TelusChannel auditCharacteristic) {
       patchCharacteristics(existingPartyIdEntity, partyIdentificationEntity, auditCharacteristic, false);
    }
    /**
     * Update a PartyIdentification and its characteristic.
     * If its characteristic don't exists, create otherwise update
     *
     * @param existingPartyIdEntity
     * @param partyIdentificationEntity
     * @param auditCharacteristic
     */
    private void patchCharacteristics(PartyIdentificationExEntity existingPartyIdEntity, PartyIdentificationExEntity partyIdentificationEntity, TelusChannel auditCharacteristic, boolean publishMDM) {
        String identificationUid = existingPartyIdEntity.getIdentificatonId();
        int IdUpdateCount = partyIdentificationDao.update(identificationUid, partyIdentificationEntity
                .version(existingPartyIdEntity.getVersion())
                .updatedBy(auditCharacteristic.getUserId())
                .originatorAppId(auditCharacteristic.getOriginatorAppId())
                .channelOrgId(auditCharacteristic.getChannelOrgId()));
        if (IdUpdateCount > 1) {
            //Allow transaction to complete. if there are more than on entries in DB with the same ID , all shall be updated
        	String msg="";
	    	msg= "[" ;
	    	msg = msg  + "ActualSize=" + IdUpdateCount;
	    	msg = msg  + ",ExpectedSize=" +1;
	    	msg = msg  + ",Message=partyIdentificationDao.update operation updated multiple rows for same identificationUid=" +identificationUid ;
	    	msg = msg  + "]";
        LOGGER.warn("{} Data Access Exception. {} ", ExceptionConstants.POSTGRES100,msg);              
        }else {
        	if (IdUpdateCount != 1) {
        		throw new IncorrectResultSizeDataAccessException("identificationUid=" +identificationUid + " partyIdentificationDao.update",1,IdUpdateCount);
        	}
        }

        for (IdentificationCharEntity characteristicEntity : CommonHelper.nullSafe(partyIdentificationEntity.getCharacteristic())) {
            Optional<IdentificationCharEntity> existingEntity = identificationCharDao
                    .getByIdentificationIdAndKey(identificationUid, characteristicEntity.getKey());
            if (!existingEntity.isPresent() && StringUtils.isNotBlank(characteristicEntity.getValue())) {
                identificationCharDao.insert(characteristicEntity
                        .identificatonId(identificationUid)
                        .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                        .originatorAppId(auditCharacteristic.getOriginatorAppId())
                        .channelOrgId(auditCharacteristic.getChannelOrgId()));
            } else if (existingEntity.isPresent()) {
                int count = identificationCharDao.update(existingEntity.get().getIdentificationCharId(), characteristicEntity
                        .updatedBy(auditCharacteristic.getUserId())
                        .version(existingEntity.get().getVersion())
                        .originatorAppId(auditCharacteristic.getOriginatorAppId())
                        .channelOrgId(auditCharacteristic.getChannelOrgId()));
                if (count > 1) {                    
                    //Allow transaction to complete. if there are more than on entries in DB with the same ID , all shall be updated
                	String msg="";
        	    	msg= "[" ;
        	    	msg = msg  + "ActualSize=" + IdUpdateCount;
        	    	msg = msg  + ",ExpectedSize=" +1;
        	    	msg = msg  + ",Message=identificationCharDao.update operation updated multiple rows for same identificationUid=" +identificationUid ;
        	    	msg = msg  + "]";
                LOGGER.warn("{} Data Access Exception. {} ", ExceptionConstants.POSTGRES100,msg);                  
                } else {
                   if (count != 1) {
                      throw new IncorrectResultSizeDataAccessException("identificationUid=" +identificationUid + " identificationCharDao.update",1,IdUpdateCount);  
                   }
                }
            }
        }
    }

    /**
     * Update a PartyIdentification and its characteristic hashes.
     * If its characteristic hashes don't exists, create otherwise update
     *
     * @param identificationUid
     * @param partyIdentificationEntity PartyIdentification and its characteristic
     * @param auditCharacteristic
     */
    private void patchCharacteristicHash(String identificationUid, PartyIdentificationExEntity partyIdentificationEntity, TelusChannel auditCharacteristic) {
        for (IdentificationCharHashEntity charHashEntity : CommonHelper.nullSafe(partyIdentificationEntity.getHashedCharacteristics())) {
            Optional<IdentificationCharHashEntity> existingEntity = identificationCharHashDao
                    .getByIdentificationIdAndKey(identificationUid, charHashEntity.getKey());
            if (!existingEntity.isPresent() && StringUtils.isNotBlank(charHashEntity.getValue())) {
                identificationCharHashDao.insert(charHashEntity
                        .identificatonId(identificationUid)
                        .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                        .originatorAppId(auditCharacteristic.getOriginatorAppId())
                        .channelOrgId(auditCharacteristic.getChannelOrgId()));
            } else if (existingEntity.isPresent()) {
                int count = identificationCharHashDao.update(existingEntity.get().getIdentificationHashId(), charHashEntity
                        .updatedBy(auditCharacteristic.getUserId())
                        .version(existingEntity.get().getVersion())
                        .originatorAppId(auditCharacteristic.getOriginatorAppId())
                        .channelOrgId(auditCharacteristic.getChannelOrgId()));
                if (count > 1) {                    
                    //Allow transaction to complete. if there are more than on entries in DB with the same ID , all shall be updated
                	String msg="";
        	    	msg= "[" ;
        	    	msg = msg  + "ActualSize=" + count;
        	    	msg = msg  + ",ExpectedSize=" +1;
        	    	msg = msg  + ",Message=identificationCharHashDao.update operation updated multiple rows for same identificationUid=" +identificationUid ;
        	    	msg = msg  + "]";  
        	    	LOGGER.warn("{} Data Access Exception. {} ", ExceptionConstants.POSTGRES100,msg); 
                } else {
                   if (count != 1) {
                      throw new IncorrectResultSizeDataAccessException("identificationUid=" +identificationUid + " identificationCharHashDao.update",1,count);  
                   }
                }
            }
        }
    }
}
