package com.telus.credit.capi.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telus.credit.capi.model.Account;
import com.telus.credit.capi.model.CreditCheckChangeEvent;
import com.telus.credit.capi.model.CreditCheckResult;
import com.telus.credit.model.AccountInfo;
import com.telus.credit.model.ContactMedium;
import com.telus.credit.model.CustomerToPatch;
import com.telus.credit.model.MediumCharacteristic;
import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusAuditCharacteristic;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.model.TelusCreditProfileCharacteristic;
import com.telus.credit.model.common.PartyType;

/*
 *For CreditCheck change events published by Wireless CAPI applications.
 * */
public class CreditCheckChangeEventMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreditCheckChangeEventMapper.class);
    private final static String WLS_LINE_OF_BUSINESS="WIRELESS";;
    private static final List<String> ORGANIZATION = Arrays.asList("B/1", "B/4", "B/A",
    		             "B/B", "B/F", "B/O", "B/R", "B/X", "C/Y", "C/1", "C/2", "C/3",
    		             "C/4", "C/5", "C/6", "C/7", "C/8", "C/9", "C/A", "C/B", "C/C",
    		             "B/D", "C/D", "C/F", "C/G", "C/H", "C/J", "C/K", "C/L", "C/M",
    		             "C/N", "B/M", "C/O", "C/Q", "C/R", "C/S", "C/T", "C/U", "E/R",
    		             "C/V", "C/W", "C/X", "C/Z", "B/W");

    public static CustomerToPatch fromEvent(CreditCheckChangeEvent event) {
    	Long customerId =event.getCustomerId();
        CustomerToPatch customerToPatch = new CustomerToPatch();
        
        TelusAuditCharacteristic auditCharacteristic = new TelusAuditCharacteristic();
        auditCharacteristic.setOriginatorApplicationId(event.getAuditInfo().getOriginatorApplicationId());
        auditCharacteristic.setUserId(event.getAuditInfo().getKbUserId());
        customerToPatch.setTelusAuditCharacteristic(auditCharacteristic);

        CreditCheckResult creditCheckResult = event.getCreditCheckResult();
        {
            TelusCreditProfile cp = new TelusCreditProfile();
            customerToPatch.setCreditProfile(Collections.singletonList(cp));   
            
            //ph1 TelusCreditProfileCharacteristic
            TelusCreditProfileCharacteristic cpCharacteristic = new TelusCreditProfileCharacteristic();
            if (creditCheckResult != null) {
            	cpCharacteristic.setCreditClassCd(creditCheckResult.getCreditClass());
                if (StringUtils.isNotBlank(creditCheckResult.getCreditDecisionMessage())) {
	                cpCharacteristic.setBureauDecisionCode(StringUtils.left(creditCheckResult.getCreditDecisionMessage(), 3));
	            }    
	            if (!CollectionUtils.isEmpty(creditCheckResult.getDepositList())) {
	                cpCharacteristic.setAverageSecurityDepositAmt(creditCheckResult.getDepositList().get(0).getDepositAmount());
	            } 
	            setCreditProramDetails(cpCharacteristic, creditCheckResult);
	            cp.setPrimaryCreditScoreCd(Objects.toString(creditCheckResult.getCreditScoreNum(), null));
	            if (StringUtils.isNotBlank(creditCheckResult.getCreditDecisionMessage())) {
	            	cp.setBureauDecisionCode(StringUtils.left(creditCheckResult.getCreditDecisionMessage(), 3));
	            }  
	            //populate CreditProramDetails
	            setCreditProramDetails(cp, creditCheckResult);
            }
            cp.setTelusCharacteristic(cpCharacteristic);

            //populate common creditprofile attaributes
            cp.setLineOfBusiness(WLS_LINE_OF_BUSINESS);
            cp.setCustomerCreditProfileRelCd("PRI");

            //ph2 populated relatedParty for owner customer 
            RelatedParty customerRelatedParty = new RelatedParty();
    		cp.setRelatedParties(Collections.singletonList(customerRelatedParty));		
    		customerRelatedParty.setId(String.valueOf(customerId));
    		customerRelatedParty.setRole("customer");
    		
            //populate EngagedParty
            setEngagedParty(customerRelatedParty, event);	
            
            //Ph2.1
            //channel
            TelusChannel channel=new TelusChannel();
            channel.setOriginatorAppId(event.getAuditInfo().getOriginatorApplicationId());
            channel.setUserId(event.getAuditInfo().getKbUserId());
            cp.setChannel(channel);
        }

		
        return customerToPatch;
    }
    
    
	private static void setEngagedParty(RelatedParty customerRelatedParty, CreditCheckChangeEvent event) {
		
		 if(event==null || event.getAccount()==null) {
			 return ;
		 }
		 
	        RelatedPartyToPatch engagedParty = new RelatedPartyToPatch();
	        customerRelatedParty.setEngagedParty(engagedParty);
	        Account accountInfo = event.getAccount();
	        String accountType = accountInfo.getAccountType() + "/" + event.getAccount().getAccountSubType();
	        if (ORGANIZATION.contains(accountType)) {
	            engagedParty.setAtReferredType(PartyType.ORGANIZATION.getType());
	            engagedParty.setRole("customer"); 
				engagedParty.setContactMedium(populateContactMediumList(event));
		        // engagedParty.setBirthDate(accountInfo.getBirthDate());  //currently CAPI doesn't provide 
	            //engagedParty.setOrganizationIdentification(accountInfo.getOrganizationIdentification());	//currently CAPI doesn't provide 
	            
	        } else {
	            engagedParty.setAtReferredType(PartyType.INDIVIDUAL.getType()); 	
	            engagedParty.setRole("customer"); 
	            engagedParty.setContactMedium(populateContactMediumList(event));
		        // engagedParty.setBirthDate(accountInfo.getBirthDate());  //currently CAPI doesn't provide 
	            //engagedParty.setOrganizationIdentification(accountInfo.getOrganizationIdentification());	
	        }	        	
	}


	private static  List<ContactMedium> populateContactMediumList(CreditCheckChangeEvent event) {
		
		 List<ContactMedium> contactMediumList = new ArrayList<ContactMedium> ();
		 String email = (event!=null && event.getAccount()!=null && event.getAccount().getEmail()!=null)?event.getAccount().getEmail():"";		 
		 if(!email.isEmpty()) {
			ContactMedium contactMedium = new ContactMedium();
	        MediumCharacteristic mediumCharacteristic = new MediumCharacteristic();
	        contactMedium.setCharacteristic(mediumCharacteristic);
	        contactMedium.setMediumType("email");
	        mediumCharacteristic.setEmail(event.getAccount().getEmail());
	        contactMediumList.add(contactMedium);
		 }
        
        return contactMediumList;
	}

    private static void setCreditProramDetails(TelusCreditProfile cp, CreditCheckResult creditCheckResult) {
    	cp.setCreditClassCd(creditCheckResult.getCreditClass());
        LOGGER.info("Mapping credit class: {}", cp.getCreditClassCd());

        switch (StringUtils.trimToEmpty(cp.getCreditClassCd()).toLowerCase()) {
            case "l":
            case "x":
                cp.setCreditProgramName("CLP");
                cp.setClpCreditLimitAmt(creditCheckResult.getCreditLimitAmt());
                cp.setAverageSecurityDepositAmt(null);
                LOGGER.info("Program CLP set, credit limit: {}", cp.getClpCreditLimitAmt());
                break;
            case "k":
                cp.setCreditProgramName("DCL");
                cp.setClpContractTerm(null);
                cp.setClpRatePlanAmt(null);
                cp.setClpCreditLimitAmt(null);
                if (!CollectionUtils.isEmpty(creditCheckResult.getDepositList())) {
                	cp.setAverageSecurityDepositAmt(creditCheckResult.getDepositList().get(0).getDepositAmount());
                }                  
                LOGGER.info("Program DCL set, deposit amount: {}", cp.getAverageSecurityDepositAmt());
                break;
            default:
                cp.setCreditProgramName("DEP");
                cp.setClpContractTerm(null);
                cp.setClpRatePlanAmt(null);
                cp.setClpCreditLimitAmt(null);
                if (!CollectionUtils.isEmpty(creditCheckResult.getDepositList())) {
                	cp.setAverageSecurityDepositAmt(creditCheckResult.getDepositList().get(0).getDepositAmount());
                }                  
                LOGGER.info("Program DEP set, deposit amount: {}", cp.getAverageSecurityDepositAmt() );
                break;
        }
    }

    private static void setCreditProramDetails(TelusCreditProfileCharacteristic cpCharacteristics, CreditCheckResult creditCheckResult) {
        LOGGER.info("Mapping credit class: {}", cpCharacteristics.getCreditClassCd());

        switch (StringUtils.trimToEmpty(cpCharacteristics.getCreditClassCd()).toLowerCase()) {
            case "l":
            case "x":
                cpCharacteristics.setCreditProgramName("CLP");
                cpCharacteristics.setClpCreditLimit(creditCheckResult.getCreditLimitAmt());
                cpCharacteristics.setAverageSecurityDepositAmt(null);
                LOGGER.info("Program CLP set, credit limit: {}", cpCharacteristics.getClpCreditLimit());
                break;
            case "k":
                cpCharacteristics.setCreditProgramName("DCL");
                cpCharacteristics.setClpContractTerm(null);
                cpCharacteristics.setClpRatePlanAmt(null);
                cpCharacteristics.setClpCreditLimit(null);
                if (!CollectionUtils.isEmpty(creditCheckResult.getDepositList())) {
                	cpCharacteristics.setAverageSecurityDepositAmt(creditCheckResult.getDepositList().get(0).getDepositAmount());
                }                
                LOGGER.info("Program DCL set, deposit amount: {}", cpCharacteristics.getAverageSecurityDepositAmt());
                break;
            default:
                cpCharacteristics.setCreditProgramName("DEP");
                cpCharacteristics.setClpContractTerm(null);
                cpCharacteristics.setClpRatePlanAmt(null);
                cpCharacteristics.setClpCreditLimit(null);
                if (!CollectionUtils.isEmpty(creditCheckResult.getDepositList())) {
                	cpCharacteristics.setAverageSecurityDepositAmt(creditCheckResult.getDepositList().get(0).getDepositAmount());
                }                
                LOGGER.info("Program DEP set, deposit amount: {}", cpCharacteristics.getAverageSecurityDepositAmt() );
                break;
        }
    }
    
    public static AccountInfo fromEvent(Account account) {
        AccountInfo accountInfo = new AccountInfo();

        if (account == null) {
            return accountInfo;
        }

        accountInfo.setAccountSubType(account.getAccountSubType());
        accountInfo.setAccountType(account.getAccountType());
        accountInfo.setBrandId(account.getBrandId() != null ? Integer.toString(account.getBrandId()) : null);
        accountInfo.setStatus(account.getStatus());
        
        accountInfo.setFirstName(account.getFirstName());
        accountInfo.setLastName(account.getLastName());      
        
        accountInfo.setLanguage(account.getLanguage()); 

        return accountInfo;
    }
}
