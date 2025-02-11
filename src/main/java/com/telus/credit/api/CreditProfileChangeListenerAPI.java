package com.telus.credit.api;

import static com.telus.credit.common.CreditMgmtCommonConstants.DEBUG_CONTEXT;
import static com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_1000;
import static com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_1000_MSG;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telus.credit.common.CreditMgmtCommonConstants;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.common.RequestContext;
import com.telus.credit.exceptions.CreditException;
import com.telus.credit.model.CreditProfile;
import com.telus.credit.model.Customer;
import com.telus.credit.model.ErrorResponse;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.pubsub.model.TelusCreditProfilePubSubEvent;
import com.telus.credit.service.impl.CreditProfileChangedHandlerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(tags = { "Customer Credit Profile Change Listener" }, produces = "application/json")
@RequestMapping(produces = "application/json")
@Validated
public class CreditProfileChangeListenerAPI {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreditProfileChangeListenerAPI.class);
   @Autowired
   private CreditProfileChangedHandlerService creditProfileChangedHandlerService;

    @PostMapping(path = "/listener/v1/customer/creditProfileChangeEvent")
    @ApiOperation(value = "Receive and updates the customer credit profile for the given customer Id", response = Customer.class)
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_ACCEPT_LANG, required = false, value = "Language preference.", allowableValues = "en, fr", defaultValue = "en", example = "en", paramType = "header"),
            @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_AUTHORIZATION, required = false, value = "Bearer token", example = "eyJhbGciOiJI...", paramType = "header"),
            @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_CORR_ID, required = false, value = "Correlation Id, which is a UUID. Used for tracing.", example = "2e15baa3-d272-11e7-a479-05de8af2b6bd", paramType = "header") })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Customer.class),
            @ApiResponse(code = 400, message = "Bad Request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unknown exception occurred.", response = ErrorResponse.class) })
    public ResponseEntity<CreditProfile>      
    	newCreditProfileEventPOST(
    				HttpServletRequest request, 
    				@ApiParam(value = "Event", required=true) @RequestBody TelusCreditProfilePubSubEvent event) {
        
        LOGGER.info("start newCreditProfileChangeEventPOST ");
        Long custId = (event!=null && event.getEvent()!=null && event.getEvent().getRelatedParties() !=null)? Long.parseLong(event.getEvent().getRelatedParties().get(0).getId()) :null;
        MDC.put(DEBUG_CONTEXT, "CustId=" + custId);
        LOGGER.info("CustId={}. Start newCreditProfileChangeEvent ", custId);
        String eventStr = (event!=null)?("["+event.toString()+"]"):"";
        String eventDesc = (event!=null)?event.getDescription():"";
        long eventReceivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
       
        long submitterEventTime = System.currentTimeMillis();//DateTimeUtils.getRequestReceivedEventTimeInMillis((event!=null)? event.getEventTime():"");
        LOGGER.info("CustId={}.event.description={}.submitterEventTime={}.receivedTime={}.newCreditProfileEventPOST_PubSubEvent:{}", custId,eventDesc,submitterEventTime,eventReceivedTime,eventStr);
        RequestContext requestContext = new RequestContext(request);
        if (!creditProfileChangedHandlerService.validate(event)) 
        {
            throw new CreditException(HttpStatus.BAD_REQUEST, ERR_CODE_1000, "Request validation failed", ERR_CODE_1000_MSG , eventStr);
        }

        if (StringUtils.isNotBlank(event.getCorrelationId())) {
            requestContext.setCorrId(event.getCorrelationId());
        }
        
        creditProfileChangedHandlerService.processCreditProfileEventAsCustomer(event.getEvent(), requestContext, eventReceivedTime,submitterEventTime,event.getDescription(),event.getEventType());
        
        TelusCreditProfile cp = event.getEvent();
        LOGGER.info("CustId={}. End newCreditProfileChangeEvent ", custId);
        
        ResponseEntity<CreditProfile> response = new ResponseEntity<>(cp, HttpStatus.OK);
        return response;
    }    
    
}
