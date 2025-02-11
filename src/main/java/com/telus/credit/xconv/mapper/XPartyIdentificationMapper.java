package com.telus.credit.xconv.mapper;

import static com.telus.credit.model.mapper.IdentificationModelMapper.COUNTRY_CD;
import static com.telus.credit.model.mapper.IdentificationModelMapper.IDENTIFICATION_ID;
import static com.telus.credit.model.mapper.IdentificationModelMapper.ISSUING_DATE;
import static com.telus.credit.model.mapper.IdentificationModelMapper.PROVINCE_CD;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.dao.entity.IdentificationCharEntity;
import com.telus.credit.dao.entity.IdentificationCharHashEntity;
import com.telus.credit.dao.entity.PartyIdentificationExEntity;
import com.telus.credit.dao.entity.XCreditProfileEntity;
import com.telus.credit.model.OrganizationIdentification;
import com.telus.credit.model.TelusIndividualIdentification;
import com.telus.credit.model.TelusIndividualIdentificationCharacteristic;
import com.telus.credit.model.TimePeriod;
import com.telus.credit.model.common.IdentificationType;
import com.telus.credit.model.mapper.IdentificationCharacteristicMapper;

@Component
public class XPartyIdentificationMapper {

    private static final String CLEARTEXT = "CLEARTEXT";

    private static CryptoService cryptoService;

    private static boolean nullOnDecryptError = false;

    @Autowired
    public void setNullOnDecryptError(@Value("${crypto.decrypt.nullOnError:false}") Boolean value) {
        XPartyIdentificationMapper.nullOnDecryptError = true;
    }

    @Autowired
    public void setCryptoService(CryptoService cryptoService) {
        XPartyIdentificationMapper.cryptoService = cryptoService;
    }

    private static PartyIdentificationExEntity newEntity(XCreditProfileEntity xcpe, String actualPartyId) {
        PartyIdentificationExEntity partyIdentificationExEntity = new PartyIdentificationExEntity();
        partyIdentificationExEntity
                .partyId(actualPartyId)
                .originatorAppId(xcpe.getOriginatorAppId())
                .channelOrgId(xcpe.getChannelOrgId())
                .createdBy(xcpe.getCreatedBy())
                .createdTs(xcpe.getCreatedTs())
                .updatedBy(xcpe.getUpdatedBy())
                .updatedTs(xcpe.getUpdatedTs());

        return partyIdentificationExEntity;
    }

    public static List<PartyIdentificationExEntity> toPartyIdentificationEntity(XCreditProfileEntity xcpe, String actualPartyId, List<String> encryptedAttrs) throws Exception {

        List<PartyIdentificationExEntity> entities = new ArrayList<>();

        if (StringUtils.isNotBlank(xcpe.getDlNum())) {
            PartyIdentificationExEntity partyIdentificationExEntity = newEntity(xcpe, actualPartyId);
            entities.add(partyIdentificationExEntity);
            mapDl(partyIdentificationExEntity, xcpe, encryptedAttrs);
        }

        if (StringUtils.isNotBlank(xcpe.getPspNum())) {
            PartyIdentificationExEntity partyIdentificationExEntity = newEntity(xcpe, actualPartyId);
            entities.add(partyIdentificationExEntity);
            mapPsp(partyIdentificationExEntity, xcpe, encryptedAttrs);
        }

        if (StringUtils.isNotBlank(xcpe.getCcTokentext())) {
            PartyIdentificationExEntity partyIdentificationExEntity = newEntity(xcpe, actualPartyId);
            entities.add(partyIdentificationExEntity);
            mapCc(partyIdentificationExEntity, xcpe, encryptedAttrs);
        }

        if (StringUtils.isNotBlank(xcpe.getHcNum())) {
            PartyIdentificationExEntity partyIdentificationExEntity = newEntity(xcpe, actualPartyId);
            entities.add(partyIdentificationExEntity);
            mapHc(partyIdentificationExEntity, xcpe, encryptedAttrs);
        }

        if (StringUtils.isNotBlank(xcpe.getPrvNum())) {
            PartyIdentificationExEntity partyIdentificationExEntity = newEntity(xcpe, actualPartyId);
            entities.add(partyIdentificationExEntity);
            mapPrv(partyIdentificationExEntity, xcpe, encryptedAttrs);
        }

        if (StringUtils.isNotBlank(xcpe.getSinNum())) {
            PartyIdentificationExEntity partyIdentificationExEntity = newEntity(xcpe, actualPartyId);
            entities.add(partyIdentificationExEntity);
            mapSin(partyIdentificationExEntity, xcpe, encryptedAttrs);
        }

        if (StringUtils.isNotBlank(xcpe.getBicNum())) {
            PartyIdentificationExEntity partyIdentificationExEntity = newEntity(xcpe, actualPartyId);
            entities.add(partyIdentificationExEntity);
            mapBic(partyIdentificationExEntity, xcpe, encryptedAttrs);
        }

        if (StringUtils.isNotBlank(xcpe.getCraNum())) {
            PartyIdentificationExEntity partyIdentificationExEntity = newEntity(xcpe, actualPartyId);
            entities.add(partyIdentificationExEntity);
            mapCra(partyIdentificationExEntity, xcpe, encryptedAttrs);
        }

        if (StringUtils.isNotBlank(xcpe.getNsjNum())) {
            PartyIdentificationExEntity partyIdentificationExEntity = newEntity(xcpe, actualPartyId);
            entities.add(partyIdentificationExEntity);
            mapNsj(partyIdentificationExEntity, xcpe, encryptedAttrs);
        }

        if (StringUtils.isNotBlank(xcpe.getQstNum())) {
            PartyIdentificationExEntity partyIdentificationExEntity = newEntity(xcpe, actualPartyId);
            entities.add(partyIdentificationExEntity);
            mapQst(partyIdentificationExEntity, xcpe, encryptedAttrs);
        }

        return entities;
    }
    public static List<OrganizationIdentification> toOrganizationIdentification(XCreditProfileEntity xcpe) throws Exception  {

        List<OrganizationIdentification> entities = new ArrayList<>();

        if (StringUtils.isNotBlank(xcpe.getDlNum())) {
        	String val = decrypt(xcpe.getDlNum());
        	OrganizationIdentification telusIndividualIdentification = new OrganizationIdentification();           
            telusIndividualIdentification.setIdentificationType(IdentificationType.DL.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getDlStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getDlStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getDlExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
        	entities.add(telusIndividualIdentification);
        }

        if (StringUtils.isNotBlank(xcpe.getPspNum())) {
        	String val = decrypt(xcpe.getPspNum());
        	OrganizationIdentification telusIndividualIdentification = new OrganizationIdentification();
            
            telusIndividualIdentification.setIdentificationType(IdentificationType.PSP.name()); 

        	
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getPspStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getPspStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getPspExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );

        	entities.add(telusIndividualIdentification);            
        }

        if (StringUtils.isNotBlank(xcpe.getCcTokentext())) {
        	String val = decrypt(xcpe.getCcTokentext());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);
            
            telusIndividualIdentification.setIdentificationType(IdentificationType.CC.name()); 

        	
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getCcStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getCcStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getCcExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
    		

        }

        if (StringUtils.isNotBlank(xcpe.getHcNum())) {
        	String val = decrypt(xcpe.getHcNum());
        	OrganizationIdentification telusIndividualIdentification = new OrganizationIdentification();

            telusIndividualIdentification.setIdentificationType(IdentificationType.HC.name()); 

        	
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getHcStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getCcStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getHcExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
 
        }

        if (StringUtils.isNotBlank(xcpe.getPrvNum())) {
        	String val = decrypt(xcpe.getPrvNum());
        	OrganizationIdentification telusIndividualIdentification = new OrganizationIdentification();
       	
            telusIndividualIdentification.setIdentificationType(IdentificationType.PRV.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getPrvStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getPrvStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getPrvExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
 
        }

        if (StringUtils.isNotBlank(xcpe.getSinNum())) {
        	String val = decrypt(xcpe.getSinNum());
        	OrganizationIdentification telusIndividualIdentification = new OrganizationIdentification();
        	
            telusIndividualIdentification.setIdentificationType(IdentificationType.SIN.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getSinStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getSinStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getSinExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );

            
        }

        if (StringUtils.isNotBlank(xcpe.getBicNum())) {
        	String val = decrypt(xcpe.getBicNum());
        	OrganizationIdentification telusIndividualIdentification = new OrganizationIdentification();

            telusIndividualIdentification.setIdentificationType(IdentificationType.BIC.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getBicStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getBicStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getBicExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );

        }

        if (StringUtils.isNotBlank(xcpe.getCraNum())) {
        	String val = decrypt(xcpe.getCraNum());
        	OrganizationIdentification telusIndividualIdentification = new OrganizationIdentification();

            telusIndividualIdentification.setIdentificationType(IdentificationType.CRA.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getCraStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getCraStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getCraExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		

        }

        if (StringUtils.isNotBlank(xcpe.getNsjNum())) {
        	String val = decrypt(xcpe.getNsjNum());
        	OrganizationIdentification telusIndividualIdentification = new OrganizationIdentification();
   
            telusIndividualIdentification.setIdentificationType(IdentificationType.NSJ.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getNsjStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getNsjStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getNsjExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
 
        }

        if (StringUtils.isNotBlank(xcpe.getQstNum())) {
        	String val = decrypt(xcpe.getQstNum());
        	OrganizationIdentification telusIndividualIdentification = new OrganizationIdentification();
  
            telusIndividualIdentification.setIdentificationType(IdentificationType.QST.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getQstStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getNsjStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getQstExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		

        }

        return entities;
    }
   
    public static List<TelusIndividualIdentification> toIndividualPartyIdentifications(XCreditProfileEntity xcpe) throws Exception  {

        List<TelusIndividualIdentification> entities = new ArrayList<>();

        if (StringUtils.isNotBlank(  xcpe.getDlNum() )) {
        	String val = decrypt(xcpe.getDlNum());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);
            
            telusIndividualIdentification.setIdentificationType(IdentificationType.DL.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getDlStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getDlStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getDlExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
            
            TelusIndividualIdentificationCharacteristic telusCharacteristic= new TelusIndividualIdentificationCharacteristic();
            telusCharacteristic.setProvinceCd(xcpe.getDlProvinceCd());        
            telusIndividualIdentification.setTelusCharacteristic(telusCharacteristic);
            
        	entities.add(telusIndividualIdentification);
        }

        if (StringUtils.isNotBlank(xcpe.getPspNum())) {
        	String val = decrypt(xcpe.getPspNum());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);
            
            telusIndividualIdentification.setIdentificationType(IdentificationType.PSP.name()); 

        	
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getPspStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getPspStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getPspExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
    		
            TelusIndividualIdentificationCharacteristic telusCharacteristic= new TelusIndividualIdentificationCharacteristic();
            telusCharacteristic.setProvinceCd(xcpe.getPspProvinceCd());        
            telusCharacteristic.setCountryCd(xcpe.getPspCountryCd());  
            telusIndividualIdentification.setTelusCharacteristic(telusCharacteristic);

        	entities.add(telusIndividualIdentification);            
        }

        if (StringUtils.isNotBlank(xcpe.getCcTokentext())) {
        	String val = decrypt(xcpe.getCcTokentext());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);
            
            telusIndividualIdentification.setIdentificationType(IdentificationType.CC.name()); 

        	
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getCcStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getCcStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getCcExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
    		
            TelusIndividualIdentificationCharacteristic telusCharacteristic= new TelusIndividualIdentificationCharacteristic();
            telusCharacteristic.setProvinceCd(xcpe.getCcProvinceCd());        
            telusCharacteristic.setCountryCd(xcpe.getCcCountryCd());  
            telusIndividualIdentification.setTelusCharacteristic(telusCharacteristic);
        	entities.add(telusIndividualIdentification);  
        }

        if (StringUtils.isNotBlank(xcpe.getHcNum())) {
        	String val = decrypt(xcpe.getHcNum());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);

            telusIndividualIdentification.setIdentificationType(IdentificationType.HC.name()); 

        	
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getHcStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getCcStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getHcExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
    		
            TelusIndividualIdentificationCharacteristic telusCharacteristic= new TelusIndividualIdentificationCharacteristic();
            telusCharacteristic.setProvinceCd(xcpe.getHcProvinceCd());        
            telusCharacteristic.setCountryCd(xcpe.getHcCountryCd());  
            telusIndividualIdentification.setTelusCharacteristic(telusCharacteristic);
        	entities.add(telusIndividualIdentification);  
        }

        if (StringUtils.isNotBlank(xcpe.getPrvNum())) {
        	String val = decrypt(xcpe.getPrvNum());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);
       	
            telusIndividualIdentification.setIdentificationType(IdentificationType.PRV.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getPrvStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getPrvStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getPrvExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
    		
            TelusIndividualIdentificationCharacteristic telusCharacteristic= new TelusIndividualIdentificationCharacteristic();
            telusCharacteristic.setProvinceCd(xcpe.getPrvProvinceCd());        
            telusCharacteristic.setCountryCd(xcpe.getPrvCountryCd());  
            telusIndividualIdentification.setTelusCharacteristic(telusCharacteristic);
        	entities.add(telusIndividualIdentification);  
        }

        if (StringUtils.isNotBlank(xcpe.getSinNum())) {
        	String val = decrypt(xcpe.getSinNum());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);

            telusIndividualIdentification.setIdentificationType(IdentificationType.SIN.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getSinStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getSinStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getSinExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
    		
            TelusIndividualIdentificationCharacteristic telusCharacteristic= new TelusIndividualIdentificationCharacteristic();
            telusCharacteristic.setProvinceCd(xcpe.getSinProvinceCd());        
            telusCharacteristic.setCountryCd(xcpe.getSinCountryCd());  
            telusIndividualIdentification.setTelusCharacteristic(telusCharacteristic);
        	entities.add(telusIndividualIdentification);  
            
        }

        if (StringUtils.isNotBlank(xcpe.getBicNum())) {
        	String val = decrypt(xcpe.getBicNum());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);

            telusIndividualIdentification.setIdentificationType(IdentificationType.BIC.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getBicStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getBicStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getBicExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
    		
            TelusIndividualIdentificationCharacteristic telusCharacteristic= new TelusIndividualIdentificationCharacteristic();
            telusCharacteristic.setProvinceCd(xcpe.getBicProvinceCd());        
            telusCharacteristic.setCountryCd(xcpe.getBicCountryCd());  
            telusIndividualIdentification.setTelusCharacteristic(telusCharacteristic);
        	entities.add(telusIndividualIdentification);  
            
        }

        if (StringUtils.isNotBlank(xcpe.getCraNum())) {
        	String val = decrypt(xcpe.getCraNum());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);

            telusIndividualIdentification.setIdentificationType(IdentificationType.CRA.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getCraStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getCraStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getCraExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
    		
            TelusIndividualIdentificationCharacteristic telusCharacteristic= new TelusIndividualIdentificationCharacteristic();
            telusCharacteristic.setProvinceCd(xcpe.getCraProvinceCd());        
            telusCharacteristic.setCountryCd(xcpe.getCraCountryCd());  
            telusIndividualIdentification.setTelusCharacteristic(telusCharacteristic);
        	entities.add(telusIndividualIdentification);  
        }

        if (StringUtils.isNotBlank(xcpe.getNsjNum())) {
        	String val = decrypt(xcpe.getNsjNum());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);
   
            telusIndividualIdentification.setIdentificationType(IdentificationType.NSJ.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getNsjStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getNsjStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getNsjExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
    		
            TelusIndividualIdentificationCharacteristic telusCharacteristic= new TelusIndividualIdentificationCharacteristic();
            telusCharacteristic.setProvinceCd(xcpe.getNsjProvinceCd());        
            telusCharacteristic.setCountryCd(xcpe.getNsjCountryCd());  
            telusIndividualIdentification.setTelusCharacteristic(telusCharacteristic);
        	entities.add(telusIndividualIdentification);  
        }

        if (StringUtils.isNotBlank(xcpe.getQstNum())) {
        	String val = decrypt(xcpe.getQstNum());
        	TelusIndividualIdentification telusIndividualIdentification = new TelusIndividualIdentification();
            telusIndividualIdentification.setType("TelusIndividualIdentification");
            telusIndividualIdentification.setBaseType("IndividualIdentification");
            //partyIdentificationExEntity.setSchemaLocation(schemaLocation);
            //partyIdentificationExEntity.setIssuingAuthority(issuingAuthority);
  
            telusIndividualIdentification.setIdentificationType(IdentificationType.QST.name()); 
            telusIndividualIdentification.setIdentificationId(val);
            telusIndividualIdentification.setIssuingDate(DateTimeUtils.toUtcDateString(xcpe.getQstStartDt()));
            TimePeriod validFor = new TimePeriod();
    		validFor.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getNsjStartDt()));
    		validFor.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getQstExpiryDt()));
    		telusIndividualIdentification.setValidFor(validFor );
    		
    		
            TelusIndividualIdentificationCharacteristic telusCharacteristic= new TelusIndividualIdentificationCharacteristic();
            telusCharacteristic.setProvinceCd(xcpe.getQstProvinceCd());        
            telusCharacteristic.setCountryCd(xcpe.getQstCountryCd());  
            telusIndividualIdentification.setTelusCharacteristic(telusCharacteristic);
        	entities.add(telusIndividualIdentification);  
        }

        return entities;
    }
    private static void mapDl(PartyIdentificationExEntity partyIdentificationExEntity, XCreditProfileEntity xcpe, List<String> encryptedAttrs) throws Exception {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        partyIdentificationExEntity.setIdType(IdentificationType.DL.name());
        partyIdentificationExEntity.setValidStartTs(xcpe.getDlStartDt());
        partyIdentificationExEntity.setValidEndTs(xcpe.getDlExpiryDt());

        IdentificationCharacteristicMapper.mapAttribute(PROVINCE_CD, xcpe.getDlProvinceCd(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
//        IdentificationCharacteristicMapper.mapAttribute(ISSUING_AUTHORITY, xcpe.getdl(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(IDENTIFICATION_ID, decrypt(xcpe.getDlNum()), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(ISSUING_DATE, DateTimeUtils.toUtcDateString(xcpe.getDlStartDt()), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        partyIdentificationExEntity.setCharacteristic(characteristicEntities);
        partyIdentificationExEntity.setHashedCharacteristics(characteristicHashEntities);
    }

    private static void mapPsp(PartyIdentificationExEntity partyIdentificationExEntity, XCreditProfileEntity xcpe, List<String> encryptedAttrs) throws Exception {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        partyIdentificationExEntity.setIdType(IdentificationType.PSP.name());
        partyIdentificationExEntity.setValidStartTs(xcpe.getPspStartDt());
        partyIdentificationExEntity.setValidEndTs(xcpe.getPspExpiryDt());

        IdentificationCharacteristicMapper.mapAttribute(COUNTRY_CD, xcpe.getDlCountryCd(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
//        IdentificationCharacteristicMapper.mapAttribute(ISSUING_AUTHORITY, xcpe.getdl(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(IDENTIFICATION_ID, decrypt(xcpe.getPspNum()), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(ISSUING_DATE, DateTimeUtils.toUtcDateString(xcpe.getPspStartDt()), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        partyIdentificationExEntity.setCharacteristic(characteristicEntities);
        partyIdentificationExEntity.setHashedCharacteristics(characteristicHashEntities);
    }

    private static void mapCc(PartyIdentificationExEntity partyIdentificationExEntity, XCreditProfileEntity xcpe, List<String> encryptedAttrs) throws Exception {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        partyIdentificationExEntity.setIdType(IdentificationType.CC.name());
        partyIdentificationExEntity.setValidStartTs(xcpe.getCcStartDt());
        partyIdentificationExEntity.setValidEndTs(xcpe.getCcExpiryDt());

//        IdentificationCharacteristicMapper.mapAttribute(ISSUING_AUTHORITY, xcpe.getdl(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(IDENTIFICATION_ID, decrypt(xcpe.getCcTokentext()), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(ISSUING_DATE, DateTimeUtils.toUtcDateString(xcpe.getCcStartDt()), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        partyIdentificationExEntity.setCharacteristic(characteristicEntities);
        partyIdentificationExEntity.setHashedCharacteristics(characteristicHashEntities);
    }

    private static void mapHc(PartyIdentificationExEntity partyIdentificationExEntity, XCreditProfileEntity xcpe, List<String> encryptedAttrs) throws Exception {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        partyIdentificationExEntity.setIdType(IdentificationType.HC.name());
        partyIdentificationExEntity.setValidStartTs(xcpe.getHcStartDt());
        partyIdentificationExEntity.setValidEndTs(xcpe.getHcExpiryDt());

//        IdentificationCharacteristicMapper.mapAttribute(ISSUING_AUTHORITY, xcpe.getdl(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(IDENTIFICATION_ID, decrypt(xcpe.getHcNum()), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(ISSUING_DATE, DateTimeUtils.toUtcDateString(xcpe.getHcStartDt()), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        partyIdentificationExEntity.setCharacteristic(characteristicEntities);
        partyIdentificationExEntity.setHashedCharacteristics(characteristicHashEntities);
    }

    private static void mapPrv(PartyIdentificationExEntity partyIdentificationExEntity, XCreditProfileEntity xcpe, List<String> encryptedAttrs) throws Exception {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        partyIdentificationExEntity.setIdType(IdentificationType.PRV.name());
        partyIdentificationExEntity.setValidStartTs(xcpe.getPrvStartDt());
        partyIdentificationExEntity.setValidEndTs(xcpe.getPrvExpiryDt());

        IdentificationCharacteristicMapper.mapAttribute(PROVINCE_CD, xcpe.getPrvProvinceCd(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
//        IdentificationCharacteristicMapper.mapAttribute(ISSUING_AUTHORITY, xcpe.getdl(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(IDENTIFICATION_ID, decrypt(xcpe.getPrvNum()), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(ISSUING_DATE, DateTimeUtils.toUtcDateString(xcpe.getPrvStartDt()), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        partyIdentificationExEntity.setCharacteristic(characteristicEntities);
        partyIdentificationExEntity.setHashedCharacteristics(characteristicHashEntities);
    }

    private static void mapSin(PartyIdentificationExEntity partyIdentificationExEntity, XCreditProfileEntity xcpe, List<String> encryptedAttrs) throws Exception {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        partyIdentificationExEntity.setIdType(IdentificationType.SIN.name());
        partyIdentificationExEntity.setValidStartTs(xcpe.getDlStartDt());
        partyIdentificationExEntity.setValidEndTs(xcpe.getDlExpiryDt());

//        IdentificationCharacteristicMapper.mapAttribute(ISSUING_AUTHORITY, xcpe.getdl(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(IDENTIFICATION_ID, decrypt(xcpe.getSinNum()), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(ISSUING_DATE, DateTimeUtils.toUtcDateString(xcpe.getSinStartDt()), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        partyIdentificationExEntity.setCharacteristic(characteristicEntities);
        partyIdentificationExEntity.setHashedCharacteristics(characteristicHashEntities);
    }

    private static void mapBic(PartyIdentificationExEntity partyIdentificationExEntity, XCreditProfileEntity xcpe, List<String> encryptedAttrs) throws Exception {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        partyIdentificationExEntity.setIdType(IdentificationType.BIC.name());
        partyIdentificationExEntity.setValidStartTs(xcpe.getDlStartDt());
        partyIdentificationExEntity.setValidEndTs(xcpe.getDlExpiryDt());

//        IdentificationCharacteristicMapper.mapAttribute(ISSUING_AUTHORITY, xcpe.getdl(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(IDENTIFICATION_ID, decrypt(xcpe.getBicNum()), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(ISSUING_DATE, DateTimeUtils.toUtcDateString(xcpe.getBicStartDt()), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        partyIdentificationExEntity.setCharacteristic(characteristicEntities);
        partyIdentificationExEntity.setHashedCharacteristics(characteristicHashEntities);
    }

    private static void mapCra(PartyIdentificationExEntity partyIdentificationExEntity, XCreditProfileEntity xcpe, List<String> encryptedAttrs) throws Exception {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        partyIdentificationExEntity.setIdType(IdentificationType.CRA.name());
        partyIdentificationExEntity.setValidStartTs(xcpe.getCraStartDt());
        partyIdentificationExEntity.setValidEndTs(xcpe.getCraExpiryDt());

//        IdentificationCharacteristicMapper.mapAttribute(ISSUING_AUTHORITY, xcpe.getdl(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(IDENTIFICATION_ID, decrypt(xcpe.getCraNum()), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(ISSUING_DATE, DateTimeUtils.toUtcDateString(xcpe.getCraStartDt()), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        partyIdentificationExEntity.setCharacteristic(characteristicEntities);
        partyIdentificationExEntity.setHashedCharacteristics(characteristicHashEntities);
    }

    private static void mapNsj(PartyIdentificationExEntity partyIdentificationExEntity, XCreditProfileEntity xcpe, List<String> encryptedAttrs) throws Exception {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        partyIdentificationExEntity.setIdType(IdentificationType.NSJ.name());
        partyIdentificationExEntity.setValidStartTs(xcpe.getNsjStartDt());
        partyIdentificationExEntity.setValidEndTs(xcpe.getNsjExpiryDt());

//        IdentificationCharacteristicMapper.mapAttribute(ISSUING_AUTHORITY, xcpe.getdl(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(IDENTIFICATION_ID, decrypt(xcpe.getNsjNum()), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(ISSUING_DATE, DateTimeUtils.toUtcDateString(xcpe.getNsjStartDt()), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        partyIdentificationExEntity.setCharacteristic(characteristicEntities);
        partyIdentificationExEntity.setHashedCharacteristics(characteristicHashEntities);
    }

    private static void mapQst(PartyIdentificationExEntity partyIdentificationExEntity, XCreditProfileEntity xcpe, List<String> encryptedAttrs) throws Exception {
        List<IdentificationCharEntity> characteristicEntities = new ArrayList<>();
        List<IdentificationCharHashEntity> characteristicHashEntities = new ArrayList<>();

        partyIdentificationExEntity.setIdType(IdentificationType.QST.name());
        partyIdentificationExEntity.setValidStartTs(xcpe.getQstStartDt());
        partyIdentificationExEntity.setValidEndTs(xcpe.getQstExpiryDt());

//        IdentificationCharacteristicMapper.mapAttribute(ISSUING_AUTHORITY, xcpe.getdl(), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(IDENTIFICATION_ID, decrypt(xcpe.getQstNum()), encryptedAttrs, characteristicEntities, characteristicHashEntities);
        IdentificationCharacteristicMapper.mapAttribute(ISSUING_DATE, DateTimeUtils.toUtcDateString(xcpe.getQstStartDt()), encryptedAttrs, characteristicEntities, characteristicHashEntities);

        partyIdentificationExEntity.setCharacteristic(characteristicEntities);
        partyIdentificationExEntity.setHashedCharacteristics(characteristicHashEntities);
    }

    private static String decrypt(String val) throws Exception {
        if (StringUtils.isBlank(val)) {
            return val;
        }

        if (val.startsWith(CLEARTEXT)) {
            return val.replaceFirst(CLEARTEXT + "\\s*", "");
        } else {
            return nullOnDecryptError ? StringUtils.trimToEmpty(cryptoService.decryptAndNullOnError(val)) : cryptoService.decrypt(val);
        }
    }
}
