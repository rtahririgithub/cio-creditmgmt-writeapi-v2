package com.telus.credit.capi.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.telus.credit.capi.mapper.CreditCheckChangeEventMapper;
import com.telus.credit.capi.model.CreditCheckChangeEvent;
import com.telus.credit.common.RequestContext;
import com.telus.credit.model.AccountInfo;
import com.telus.credit.model.CustomerToPatch;
import com.telus.credit.service.CustomerService;

@Service
public class CApiService {

    @Autowired
    private CustomerService customerService;

    public void sync(CreditCheckChangeEvent event,long receivedTime,long submitterEventTime) {
        RequestContext context = new RequestContext("en", event.getEventId(), StringUtils.EMPTY);

        CustomerToPatch customerToPatch = CreditCheckChangeEventMapper.fromEvent(event);

	        customerToPatch.getTelusAuditCharacteristic().setTenpubsubsync(true);
	        long custId = event.getAccount().getBan();
	        AccountInfo accountInfo = CreditCheckChangeEventMapper.fromEvent(event.getAccount());
	        //populate lineofbusiness as this service process messages published by wireless CAPI service.
	        customerToPatch.getCreditProfile().get(0).setLineOfBusiness("WIRELESS");
	        customerService.saveCustomerById(context, custId, customerToPatch, accountInfo, receivedTime,submitterEventTime,"");
    
    }
}
