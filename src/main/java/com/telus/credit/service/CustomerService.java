package com.telus.credit.service;

import com.telus.credit.common.RequestContext;
import com.telus.credit.model.AccountInfo;
import com.telus.credit.model.Customer;
import com.telus.credit.model.CustomerToPatch;
import com.telus.credit.model.TelusCreditProfile;

public interface CustomerService {

    Customer saveCustomerById(RequestContext requestContext, Long custId, CustomerToPatch customerToPatch, AccountInfo accountInfo, long receivedTime,long submitterEventTime, String eventDescription);

    Customer getCustomerPartyFromDatabase(Long custId);

    Customer updateCreditProfileResourceByCPId(RequestContext context, String creditProfileId, TelusCreditProfile creditProfile, AccountInfo accountInfo, long receivedTime, long submitterEventTime, String eventDescription);

    Customer createCreditProfileResource(RequestContext context, Long custId, TelusCreditProfile creditProfile,  AccountInfo accountInfo, long receivedTime,long submitterEventTime, String eventDescription);

	

}