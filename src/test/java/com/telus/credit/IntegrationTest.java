
package com.telus.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.capi.mapper.CreditCheckChangeEventMapper;
import com.telus.credit.capi.model.CreditCheckChangeEvent;
import com.telus.credit.capi.service.CApiService;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.common.RequestContext;
import com.telus.credit.config.CryptoConfig;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.crypto.service.HashService;
import com.telus.credit.dao.CreditProfileDao;
import com.telus.credit.dao.CustomerCreditProfileRelDao;
import com.telus.credit.dao.entity.CustomerCreditProfileRelEntity;
import com.telus.credit.dao.entity.CustomerEntity;
import com.telus.credit.dao.mapper.CompositeRowMapper.CompositeEntity;
import com.telus.credit.dao.rowmapper.CustomerCreditProfileRelRowMapper;
import com.telus.credit.dao.rowmapper.CustomerRowMapper;
import com.telus.credit.model.AccountInfo;
import com.telus.credit.model.Customer;
import com.telus.credit.model.CustomerToPatch;
import com.telus.credit.service.CustomerService;
import com.telus.credit.service.impl.DefaultCustomerService;
import com.telus.credit.xconv.service.XConvService;

@SpringBootTest
//@DataJdbcTest


public class IntegrationTest {
	
	@Autowired 
	CreditProfileDao aCreditProfileDao;
	
	@Autowired 
	CustomerCreditProfileRelDao aCustomerCreditProfileRelDao;	
	
	@Autowired 
	DefaultCustomerService  aDefaultCustomerService;

  @Test
  public void daoTest() {
	assertThat(aDefaultCustomerService).isNotNull();
    try {
    	Long custId = (long) 200048;
    	Customer customer = aDefaultCustomerService.getCustomerPartyFromDatabase(custId);
    	//customer.getCreditProfile().get(0).getCustomerCreditProfileRelCd()
    	
		System.out.println("done");
  

    } catch (Exception e) {
      e.printStackTrace();
      //Assertions.fail("daoTest test failed with error=" + e.getMessage());
    }
  }

  @Autowired 
  CApiService cApiService;
  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void testSync() throws Exception {
	  String testfilename;
	  testfilename="capi/capievent-individual.json";
	  testfilename="capi/capievent-prepaid.json";
	  
      CreditCheckChangeEvent event = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testfilename), CreditCheckChangeEvent.class);
      CustomerToPatch customerToPatch = CreditCheckChangeEventMapper.fromEvent(event);
      customerToPatch.getTelusAuditCharacteristic().setTenpubsubsync(true);
      AccountInfo accountInfo = CreditCheckChangeEventMapper.fromEvent(event.getAccount());
      long receivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
      try {     
    	  cApiService.sync(event,receivedTime,receivedTime);
	  } catch (Exception e) {
	      e.printStackTrace();
	      Assertions.fail("daoTest test failed with error=" + e.getMessage());
	    }      
      System.out.println("done");
  }  
  @Autowired 
  XConvService aXConvService;
  @Test
  void testconversion() throws Exception {	  
	 String conversionLineOfBusiness="WIRELINE";
	long custId=86313196;
	aXConvService.syncCustomer(custId, conversionLineOfBusiness);
  }
  
}


