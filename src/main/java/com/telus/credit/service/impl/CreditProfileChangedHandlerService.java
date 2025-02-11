package com.telus.credit.service.impl;

import static com.telus.credit.exceptions.ExceptionConstants.DATAVALIDATION100;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.telus.credit.common.CommonHelper;
import com.telus.credit.common.RequestContext;
import com.telus.credit.controllers.CreditProfileController;
import com.telus.credit.model.TelusAuditCharacteristic;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.pubsub.model.Customer;
import com.telus.credit.pubsub.model.TelusCreditProfilePubSubEvent;
import com.telus.credit.service.CreditProfileService;
import com.telus.credit.service.CustomerService;

@Service
public class CreditProfileChangedHandlerService {

	@Autowired
	CreditProfileController creditProfileController;
	   
    private static final Logger LOGGER = LoggerFactory.getLogger(CreditProfileChangedHandlerService.class);

    private CustomerService customerService;
    public CreditProfileChangedHandlerService(CustomerService customerService,
                                              CreditProfileService profileService) {
        this.customerService = customerService;
    }

    public boolean validate(TelusCreditProfilePubSubEvent event) {
//        boolean isValid = !(Objects.isNull(event)
//                || Objects.isNull(event.getEvent())
//                || Objects.isNull(event.getEvent().getCustomer())
//                || Objects.isNull(event.getEvent().getCustomer().getId()));
//
//        if (!isValid) {
//            LOGGER.warn("{} Invalid PubSubEvent {}", DATAVALIDATION100, event);
//        }

        boolean isValid = 
        		!(
        		Objects.isNull(event)
                || Objects.isNull(event.getEvent())
                || Objects.isNull(event.getEvent().getRelatedParties())
                || Objects.isNull(event.getEvent().getRelatedParties().size()==0)
                || Objects.isNull(event.getEvent().getRelatedParties().get(0).getId()==null)
                );

        if (!isValid) {
            LOGGER.warn("{} Invalid PubSubEvent {}", DATAVALIDATION100, event);
        }


        return isValid;
    }
  
    public void processCreditProfileEventAsCustomer(TelusCreditProfile telusCreditProfile, RequestContext context,  long receivedTime, long submitterEventTime,String eventDescription, String eventType) {

    	CommonHelper.refineCreditProgram(telusCreditProfile);

       
        List<TelusCreditProfile> creditProfiles = new ArrayList<TelusCreditProfile>();
        creditProfiles.add(telusCreditProfile);
        
        Customer customer = new Customer();
        String custId = telusCreditProfile.getRelatedParties().get(0).getId();
        long id = Long.parseLong(custId);
        customer.setId(id);
        customer.setCreditProfile(creditProfiles);
        
		TelusAuditCharacteristic auditCharacteristic = new TelusAuditCharacteristic();
		auditCharacteristic.setUserId(telusCreditProfile.getChannel().getUserId());
		auditCharacteristic.setChannelOrganizationId(telusCreditProfile.getChannel().getChannelOrgId());
		auditCharacteristic.setOriginatorApplicationId(telusCreditProfile.getChannel().getOriginatorAppId());
		auditCharacteristic.setTenpubsubsync(true);
		customer.setTelusAuditCharacteristic(auditCharacteristic);        
        telusCreditProfile.getChannel().setTenpubsubsync(true);

        if("UNMERGE".equalsIgnoreCase(eventType)) {
        	creditProfileController.unmergeCreditprofiles(context, String.valueOf(customer.getId()), telusCreditProfile.getChannel(), receivedTime, submitterEventTime);
        }
        else {
        	customerService.saveCustomerById(context, customer.getId(), customer, null, receivedTime,submitterEventTime,eventDescription);
        }
        
    }
/*    
    public void processCreditProfileEvent(TelusCreditProfile creditProfile, Long custId, RequestContext context, long receivedTime, long submitterEventTime, String eventDescription) {

        if (StringUtils.isEmpty(creditProfile.getId())) {
            LOGGER.info("start createCreditProfileWithoutId");
            profileService.createCreditProfileWithoutId(context, creditProfile, null, receivedTime, submitterEventTime, "");
        }
        else {
            LOGGER.info("start patchCreditProfileById for Id {}", creditProfile.getId());
            customerService.patchCreditProfileByCPId(context, creditProfile.getId(), creditProfile, null, receivedTime, submitterEventTime, eventDescription);
        }
    }
*/    

    /**
     * Refine Credit Profile Characteristics based on Credit Program Name
     *
     * @param characteristic
     */
/*    
    private void refine(TelusCreditProfileCharacteristic characteristic) {
        if (characteristic == null) {
            LOGGER.info("Characteristic is null, no refinement needed");
            return;
        }

        String creditProgramName = StringUtils.trimToEmpty(characteristic.getCreditProgramName());
        switch (creditProgramName.toLowerCase()) {
            case "clp":
                refineForClp(characteristic);
                break;
            case "ndp":
                refineForNdp(characteristic);
                break;
            case "dep":
                refineForDep(characteristic);
                break;
            default:
                LOGGER.info("Ignore refining credit program: {}", characteristic.getCreditProgramName());
                break;
        }
    }
*/
  
    /**
     * DEP contains following attributes: creditProgramName, creditClassCd, creditClassDate, creditDecisionCd,
     * creditDecisionDate, riskLevelNumber, riskLevelDecisionCd, riskLevelDt, averageSecurityDepositAmt
     *
     * @param characteristic
     */
    /*
    private void refineForDep(TelusCreditProfileCharacteristic characteristic) {
        characteristic.setClpCreditLimit(null);
        characteristic.setClpRatePlanAmt(null);
        characteristic.setClpContractTerm(null);
        LOGGER.info("Refined DEP");
    }
 */
    /**
     * CLP accepts following attributes: creditProgramName, creditClassCd, creditClassDate, creditDecisionCd,
     * creditDecisionDate, riskLevelNumber, riskLevelDecisionCd, riskLevelDt,
     * clpCreditLimit, clpRatePlanThresholdAmt, clpContractTerm
     *
     * @param characteristic
     */
    /*
    private void refineForClp(TelusCreditProfileCharacteristic characteristic) {
        characteristic.setAverageSecurityDepositAmt(null);
        LOGGER.info("Refined CLP");
    }
     */
    /**
     * NDP contains following attributes: creditProgramName, creditClassCd, creditClassDate, creditDecisionCd,
     * creditDecisionDate, riskLevelNumber, riskLevelDecisionCd, riskLevelDt
     *
     * @param characteristic
     */
    /* 
       private void refineForNdp(TelusCreditProfileCharacteristic characteristic) {
        characteristic.setAverageSecurityDepositAmt(null);
        characteristic.setClpCreditLimit(null);
        characteristic.setClpRatePlanAmt(null);
        characteristic.setClpContractTerm(null);
        LOGGER.info("Refined NDP");
    }
    
 */
    
}
