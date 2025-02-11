package com.telus.credit.controllers;

import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.telus.credit.common.RequestContext;
import com.telus.credit.model.Customer;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.service.CreditProfileService;
import com.telus.credit.service.CustomerService;
import com.telus.credit.service.MergeService;
import com.telus.credit.service.impl.DbService;

@Component
public class CreditProfileController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreditProfileController.class);

	@Autowired
	private CustomerService customerService;

	@Autowired
	private CreditProfileService creditProfileService;
	
	@Autowired
	private DbService dbService;
	
	@Autowired
	private MergeService mergeSvc; 	

	/*
	public Customer patchCustomerById(RequestContext requestContext, Long custId, CustomerToPatch customerToPatch,long receivedTime,long submitterEventTime) {
		MDC.put("debugContext", "CustId=" + custId);
		return customerService.saveCustomerById(requestContext, custId, customerToPatch, null, receivedTime, submitterEventTime,"");
	}
  */

	public Customer createCreditProfile(RequestContext requestContext, TelusCreditProfile creditProfile, long receivedTime,long submitterEventTime) {
		return creditProfileService.createCreditProfileWithoutId(requestContext, creditProfile, null, receivedTime, submitterEventTime, "");
	}
	public Customer patchCreditProfileByCPId(RequestContext requestContext, String creditProfileId, TelusCreditProfile creditProfileToPatch, long receivedTime,long submitterEventTime) {
		Customer customerInDB = customerService.updateCreditProfileResourceByCPId(requestContext, creditProfileId, creditProfileToPatch, null, receivedTime, submitterEventTime, "");
		return customerInDB;
	}
	public TelusCreditProfile unmergeCreditprofiles(RequestContext requestContext, String tobeUnmergedCustomerId,@Valid TelusChannel telusChannel, long eventReceivedTime, long submitterEventTime) {
		TelusCreditProfile unmergedCreditprofile = mergeSvc.unmergeCreditprofiles(new Long(tobeUnmergedCustomerId), telusChannel);
		return unmergedCreditprofile;
	}
	public String getVersionInfo() {
		LOGGER.info("getVersionInfo()  called:");
		Map<String, String> env = System.getenv();
		if (env != null && !env.isEmpty()) {
			env.forEach((key, val) -> {
				LOGGER.info("Env key:{} value:{}", key, val);
			});
		}

		return "baseV1 at " + dbService.getDateTime();
	}
}
