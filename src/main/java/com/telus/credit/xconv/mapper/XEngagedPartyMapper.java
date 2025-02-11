package com.telus.credit.xconv.mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.telus.credit.common.CommonHelper;
import com.telus.credit.common.CreditProfileConstant;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.dao.entity.IndividualEntity;
import com.telus.credit.dao.entity.OrganizationEntity;
import com.telus.credit.dao.entity.PartyEntity;
import com.telus.credit.dao.entity.PartyIdentificationExEntity;
import com.telus.credit.dao.entity.XCreditProfileEntity;
import com.telus.credit.model.ContactMedium;
import com.telus.credit.model.Individual;
import com.telus.credit.model.MediumCharacteristic;
import com.telus.credit.model.OrganizationIdentification;
import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusCharacteristic;
import com.telus.credit.model.TelusIndividualIdentification;
import com.telus.credit.model.TimePeriod;
import com.telus.credit.model.common.PartyType;

public class XEngagedPartyMapper {

    public static PartyEntity mapPartyEntity(XCreditProfileEntity xcpe) {
        PartyEntity partyEntity = new PartyEntity();

        String partyType = (String) CommonHelper.safeTransform(
        		xcpe.getPartyType(),
                t-> {
                    if (!(PartyType.INDIVIDUAL.getType().equals(t) || PartyType.ORGANIZATION.getType().equals(t))) {
                        throw new IllegalArgumentException(t + " must be Individual or Organization");
                    }
                    return t;
                	},
                PartyType.INDIVIDUAL.getType(),
                "Expect xcpe.getPartyType() not blank. Use default value"
                );

        partyEntity.partyId(xcpe.getPartyId())
                .partyRole((String) CommonHelper.safeTransform(xcpe.getPartyRole(), t->t, "Customer", "Expect xcpe.getPartyRole() not blank. Use default value"))
                .partyType(partyType)
                .originatorAppId(xcpe.getOriginatorAppId())
                .channelOrgId(xcpe.getChannelOrgId())
                .createdBy(xcpe.getCreatedBy())
                .createdTs(xcpe.getCreatedTs())
                .updatedBy(xcpe.getUpdatedBy())
                .updatedTs(xcpe.getUpdatedTs())
                .statusCd(xcpe.getStatus())
                ;

        return partyEntity;
    }
 
    public static IndividualEntity mapIndividualEntity(XCreditProfileEntity xcpe) {
        IndividualEntity individualEntity = new IndividualEntity();
        if (PartyType.INDIVIDUAL.getType().equals(xcpe.getReferredtype())) {
            String birthDay = StringUtils.trimToNull(xcpe.getBirthDate());
            individualEntity
            		.birthDate((Date) CommonHelper.safeTransform(birthDay, b -> DateTimeUtils.toUtcDate(b.split("\\s")[0]), null, null))
                    .originatorAppId(xcpe.getOriginatorAppId())
                    .channelOrgId(xcpe.getChannelOrgId())
                    .createdBy(xcpe.getCreatedBy())
                    .createdTs(xcpe.getCreatedTs())
                    .updatedBy(xcpe.getUpdatedBy())
                    .updatedTs(xcpe.getUpdatedTs())
                    .employmentStatusCd(xcpe.getEmploymentStatusCd())
                    .legalCareCd(xcpe.getLegalCareCd())
                    .residencyCd(xcpe.getResidencyCd())
                    .primCredCardTypCd(xcpe.getPrimCredCardTypCd())
                    .secCredCardIssCoTypCd(xcpe.getSecCredCardIssCoTypCd())                  
                    ;

            return individualEntity;
        }

        return null;
    }

    public static RelatedPartyToPatch mapEngagedParty(XCreditProfileEntity xcpe) throws Exception {
    	RelatedPartyToPatch engagedParty = new RelatedPartyToPatch();

        	engagedParty.setAtReferredType(xcpe.getReferredtype());
            engagedParty.setRole("Customer");
            String birthDay = StringUtils.trimToNull(xcpe.getBirthDate());
            engagedParty.setBirthDate(birthDay);

	        List<TelusCharacteristic> characteristicList = new LinkedList<>();
	        if(!StringUtils.isEmpty(xcpe.getEmploymentStatusCd())) {
	            TelusCharacteristic characteristic = new TelusCharacteristic();
	            characteristic.setName(CreditProfileConstant.EMPLOYMENT_STATUS_CD);
	            characteristic.setValue(xcpe.getEmploymentStatusCd());
	            characteristic.setValueType("String");
	            characteristic.setType("TelusCharacteristic");
	            characteristicList.add(characteristic);
	        }
	        if(!StringUtils.isEmpty(xcpe.getLegalCareCd())) {
	            TelusCharacteristic characteristic = new TelusCharacteristic();
	            characteristic.setName(CreditProfileConstant.LEGAL_CARE_CD);
	            characteristic.setValue(xcpe.getLegalCareCd());
	            characteristic.setValueType("String");
	            characteristic.setType("TelusCharacteristic");
	            characteristicList.add(characteristic);
	        }
	        if(!StringUtils.isEmpty(xcpe.getPrimCredCardTypCd())) {
	            TelusCharacteristic characteristic = new TelusCharacteristic();
	            characteristic.setName(CreditProfileConstant.PRIM_CRED_CARD_TYP_CD);
	            characteristic.setValue(xcpe.getPrimCredCardTypCd());
	            characteristic.setValueType("String");
	            characteristic.setType("TelusCharacteristic");
	            characteristicList.add(characteristic);
	        }
	        if(!StringUtils.isEmpty(xcpe.getResidencyCd())) {
	            TelusCharacteristic characteristic = new TelusCharacteristic();
	            characteristic.setName(CreditProfileConstant.RESIDENCY_CD);
	            characteristic.setValue(xcpe.getResidencyCd());
	            characteristic.setValueType("String");
	            characteristic.setType("TelusCharacteristic");
	            characteristicList.add(characteristic);
	        }
	        if(!StringUtils.isEmpty(xcpe.getSecCredCardIssCoTypCd())) {
	            TelusCharacteristic characteristic = new TelusCharacteristic();
	            characteristic.setName(CreditProfileConstant.SEC_CRED_CARD_ISS_CO_TYP_CD);
	            characteristic.setValue(xcpe.getSecCredCardIssCoTypCd());
	            characteristic.setValueType("String");
	            characteristic.setType("TelusCharacteristic");
	            characteristicList.add(characteristic);
	        }
	        
	        engagedParty.setCharacteristic(characteristicList);            
	        
	        //  populate identifications          
	        if (PartyType.ORGANIZATION.getType().equals(xcpe.getReferredtype())) {
	        	List<OrganizationIdentification> identifications = XPartyIdentificationMapper.toOrganizationIdentification(xcpe);
	        	engagedParty.setOrganizationIdentification(identifications);
	        }else {
		        if (PartyType.INDIVIDUAL.getType().equals(xcpe.getReferredtype())) {
		        	List<TelusIndividualIdentification> identifications = XPartyIdentificationMapper.toIndividualPartyIdentifications(xcpe);
		        	 engagedParty.setIndividualIdentification(identifications);
		        }	        
		        
	        }
	       
	        List<ContactMedium> contactMediums = new ArrayList<ContactMedium>();
	        ContactMedium contactMedium = new ContactMedium();
	        //contactMedium.setId(String);
	        contactMedium.setMediumType(xcpe.getMediumType());
	        contactMedium.setPreferred((Boolean) CommonHelper.safeTransform(xcpe.getPreffered(), v-> BooleanUtils.toBoolean(StringUtils.trim(v)), null, null));            
			TimePeriod timePeriod = new TimePeriod();
			timePeriod.setStartDateTime(DateTimeUtils.toUtcString(xcpe.getValidStartTs()));
			timePeriod.setEndDateTime(DateTimeUtils.toUtcString(xcpe.getValidEndTs()));
			contactMedium.setValidFor(timePeriod);	        
     
			 MediumCharacteristic aMediumCharacteristic = new MediumCharacteristic();
			 aMediumCharacteristic.setCity(xcpe.getCity());
			 aMediumCharacteristic.setContactType(xcpe.getContactType());
			 aMediumCharacteristic.setCountry(xcpe.getCountryCode());
			 aMediumCharacteristic.setEmail(xcpe.getContactType());
			 aMediumCharacteristic.setPhoneNumber(xcpe.getContactType());
			 aMediumCharacteristic.setPostCode(xcpe.getPostCode());
			 aMediumCharacteristic.setStateOrProvince(xcpe.getStateProvinceCode());
			 aMediumCharacteristic.setStreet1(xcpe.getStreet1());
			 aMediumCharacteristic.setStreet2(xcpe.getStreet2());
			 aMediumCharacteristic.setStreet3(xcpe.getStreet3());
			 aMediumCharacteristic.setStreet4(xcpe.getStreet4());
			 aMediumCharacteristic.setStreet5(xcpe.getStreet5());


             
			contactMedium.setCharacteristic(aMediumCharacteristic);
			contactMediums.add(contactMedium);
			engagedParty.setContactMedium(contactMediums);
	        

            return engagedParty;

    }    
    public static OrganizationEntity mapOrganizationEntity(XCreditProfileEntity xcpe) {
        OrganizationEntity organizationEntity = new OrganizationEntity();
        if (PartyType.ORGANIZATION.getType().equals(xcpe.getReferredtype())) {
            organizationEntity.originatorAppId(xcpe.getOriginatorAppId())
                    .channelOrgId(xcpe.getChannelOrgId())
                    .createdBy(xcpe.getCreatedBy())
                    .createdTs(xcpe.getCreatedTs())
                    .updatedBy(xcpe.getUpdatedBy())
                    .updatedTs(xcpe.getUpdatedTs());

            return organizationEntity;
        }

        return null;
    }
}
