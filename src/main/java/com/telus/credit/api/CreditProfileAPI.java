package com.telus.credit.api;

import static com.telus.credit.exceptions.ExceptionConstants.DATAVALIDATION100;
import static com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_1000;
import static com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_1000_MSG;
import static com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_1115;
import static com.telus.credit.exceptions.ExceptionConstants.ERR_CODE_1115_MSG;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.telus.credit.common.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.telus.credit.CreditProfileApplication;
import com.telus.credit.common.CreditMgmtCommonConstants;
import com.telus.credit.common.DateTimeUtils;
import com.telus.credit.common.RequestContext;
import com.telus.credit.controllers.CreditProfileController;
import com.telus.credit.exceptions.CreditException;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.model.CreditProfile;
import com.telus.credit.model.CreditProfileToCreate;
import com.telus.credit.model.Customer;
import com.telus.credit.model.ErrorResponse;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.validation.ValidCreditProfileId;
import com.telus.credit.validation.group.Patch;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(tags = { "Customer Credit Profile" }, produces = "application/json")
@RequestMapping(path = "/customer/creditprofile-mgmt", produces = "application/json")
@Validated
public class CreditProfileAPI {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreditProfileAPI.class);

   @Autowired
   CreditProfileController creditProfileController;

   @Autowired
   private Environment env;

   private final Bucket bucket;
   public CreditProfileAPI() {	 
	 //Rate limit how many HTTP requests can be made in a given period of 1 minute
       long capacity = 5000;
       long tokens=5000;
       Duration period = Duration.ofMinutes(1);      
       Refill speedOfTokenRegeneration = Refill.greedy(tokens, period); 
       Bandwidth limit = Bandwidth.classic(capacity, speedOfTokenRegeneration);
       this.bucket = Bucket4j.builder()
           .addLimit(limit)
           .build();
   }   

    @PostMapping("/creditProfile")
    @ApiOperation(value = "Create a new credit profile", response = CreditProfile.class)
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_ACCEPT_LANG, required = false, value = "Language preference.", allowableValues = "en, fr", defaultValue = "en", example = "en", paramType = "header"),
            @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_AUTHORIZATION, required = false, value = "Bearer token", example = "eyJhbGciOiJI...", paramType = "header"),
            @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_CORR_ID, required = false, value = "Correlation Id, which is a UUID. Used for tracing.", example = "2e15baa3-d272-11e7-a479-05de8af2b6bd", paramType = "header") })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CreditProfileToCreate.class),
            @ApiResponse(code = 400, message = "Bad Request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unknown exception occurred.", response = ErrorResponse.class) })
    public ResponseEntity<CreditProfile> createCreditProfile(HttpServletRequest request,
                                                             @RequestBody @Valid TelusCreditProfile telusCreditProfile) {
    	
    	//Rate limit how many HTTP requests can be made in a given period of 1 minute
    	//limit is  exceeded
        if (!bucket.tryConsume(1)) {
        	return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

    	validateCreateRequest(telusCreditProfile);
    	
        RequestContext requestContext = new RequestContext(request);
        TelusCreditProfile incomingCreditProfile = telusCreditProfile;
        long eventReceivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
        long submitterEventTime= DateTimeUtils.getRequestReceivedTimestampInMillis();
        
        Customer customerInDB = creditProfileController.createCreditProfile(requestContext, incomingCreditProfile, eventReceivedTime, submitterEventTime);
        
        customerInDB.getCreditProfile();
         
        ResponseEntity<CreditProfile> response = new ResponseEntity<>(incomingCreditProfile, HttpStatus.OK);
        return response;
        
    }

    @PatchMapping("/creditProfile/{id}")
    @ApiOperation(value = "Updates the customer credit profile for the given credit profile Id", response = Customer.class,notes = "Use this API to add or update a credit profile to the customer.")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_ACCEPT_LANG, required = false, value = "Language preference.", allowableValues = "en, fr", defaultValue = "en", example = "en", paramType = "header"),
            @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_AUTHORIZATION, required = false, value = "Bearer token", example = "eyJhbGciOiJI...", paramType = "header"),
            @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_CORR_ID, required = false, value = "Correlation Id, which is a UUID. Used for tracing.", example = "2e15baa3-d272-11e7-a479-05de8af2b6bd", paramType = "header") })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Customer.class),
            @ApiResponse(code = 400, message = "Bad Request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unknown exception occurred.", response = ErrorResponse.class) })
    public ResponseEntity<TelusCreditProfile> patchCreditProfileByCPId(HttpServletRequest request,
                                                                    @ApiParam(value = "Credit Profile Id", type = "string", required = true, example = "16") 
    																@PathVariable("id") @ValidCreditProfileId String creditProfileId,
                                                                    @ApiParam(value = "Customer info", required=true) 
    																@Validated(Patch.class) @RequestBody TelusCreditProfile creditProfileToPatch) 
    {

    	//Rate limit how many HTTP requests can be made in a given period of 1 minute
    	//limit is  exceeded
        if (!bucket.tryConsume(1)) {
        	return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

    	validateUpdateRequest(creditProfileToPatch);
    	
        LOGGER.info("CustId={}. Start patchCustomerCreditProfileById ", creditProfileId);
        RequestContext requestContext = new RequestContext(request);
        long eventReceivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
        long submitterEventTime= DateTimeUtils.getRequestReceivedTimestampInMillis();//TODO get evenTime from request
       
        Customer customerInDB = creditProfileController.patchCreditProfileByCPId(requestContext, creditProfileId, creditProfileToPatch, eventReceivedTime, submitterEventTime);
        TelusCreditProfile c = creditProfileToPatch;
        
        //return CreditProfile in response 
        List<TelusCreditProfile> dBCreditProfileList = customerInDB.getCreditProfile();
        for (TelusCreditProfile telusCreditProfile : dBCreditProfileList) {
        	
        	if (Objects.nonNull(creditProfileToPatch.getId()) && creditProfileToPatch.getId().equals(telusCreditProfile.getId()) ) {
        		c=telusCreditProfile;
        	} 
       	
        }       
        ResponseEntity<TelusCreditProfile> response = new ResponseEntity<>(c, HttpStatus.OK);        
        LOGGER.info("CreditProfileId={}. End patchCustomerCreditProfileById ", creditProfileId);
		return response;       
       
}


    @PostMapping(value = "/creditProfile/unmerge/")
	@ApiOperation(value = "unmerge the customer credit profile for the given customerID Id", response = TelusCreditProfile.class,notes = "Use this API to unmerge a customer.")
	@ApiImplicitParams(value = {
	        @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_ACCEPT_LANG, required = false, value = "Language preference.", allowableValues = "en, fr", defaultValue = "en", example = "en", paramType = "header"),
	        @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_AUTHORIZATION, required = false, value = "Bearer token", example = "eyJhbGciOiJI...", paramType = "header"),
	        @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_CORR_ID, required = false, value = "Correlation Id, which is a UUID. Used for tracing.", example = "2e15baa3-d272-11e7-a479-05de8af2b6bd", paramType = "header") })
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "OK", response = Customer.class),
	        @ApiResponse(code = 400, message = "Bad Request.", response = ErrorResponse.class),
	        @ApiResponse(code = 500, message = "Unknown exception occurred.", response = ErrorResponse.class) })
	public ResponseEntity<TelusCreditProfile> unmergeCustomer(
            HttpServletRequest request,
            @RequestParam(required = true) @NotEmpty(message = "1500") Map<String, String> params,
            @RequestBody @Valid TelusChannel telusChannel
    ) {
    	LOGGER.info("CustId={}. Start unmergeCustomer ", params);
    	//Rate limit how many HTTP requests can be made in a given period of 1 minute
    	//limit is  exceeded
        if (!bucket.tryConsume(1)) {
        	return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        
    	Map<String, String> newMap = params.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), entry -> entry.getValue()));
    	String tobeUnmergedCustomerId = newMap.get("CUSTOMERID");
        
    	RequestContext requestContext = new RequestContext(request);
        long eventReceivedTime = DateTimeUtils.getRequestReceivedTimestampInMillis();
        long submitterEventTime= DateTimeUtils.getRequestReceivedTimestampInMillis();
       
        TelusCreditProfile unmergedCreditProfile = creditProfileController.unmergeCreditprofiles(requestContext, tobeUnmergedCustomerId, telusChannel, eventReceivedTime, submitterEventTime);
        
        ResponseEntity<TelusCreditProfile> response = new ResponseEntity<>(unmergedCreditProfile, HttpStatus.OK);        
        LOGGER.info("CustId={}. End unmergeCustomer ", tobeUnmergedCustomerId);
		return response; 
    }
    
  private void validateCreateRequest(TelusCreditProfile creditProfile) {

      boolean isValid =
              !(
                      Objects.isNull(creditProfile)
                              || Objects.isNull(creditProfile.getRelatedParties())
                              || Objects.isNull(creditProfile.getRelatedParties().size() == 0)
                              || Objects.isNull(creditProfile.getRelatedParties().get(0).getId() == null)
              );
      if (!isValid) {
          LOGGER.warn("{} Invalid creditProfile {}", DATAVALIDATION100, creditProfile);
          // throw new CreditException(HttpStatus.BAD_REQUEST, ERR_CODE_1000, "Request validation failed", ERR_CODE_1000_MSG , (creditProfile!=null)?creditProfile.toString():"");
          throw new CreditException(HttpStatus.BAD_REQUEST, ERR_CODE_1000, "Missing  relatedparty with customer role", ERR_CODE_1000_MSG, (creditProfile != null) ? creditProfile.toString() : "");
      }

      long aCustId = creditProfile.getRelatedPartyCustomerRoleCustId();
      if (aCustId == 0) {
          throw new CreditException(HttpStatus.BAD_REQUEST, ERR_CODE_1000, "Missing  relatedparty with customer role", ERR_CODE_1000_MSG, (creditProfile != null) ? creditProfile.toString() : "");

      }
      String creditClassCd = creditProfile.getCreditClassCd();
      if (StringUtils.isBlank(creditClassCd)) {
          throw new CreditException(HttpStatus.BAD_REQUEST, ERR_CODE_1115, "Missing / Invalid Credit Class CD", ERR_CODE_1115_MSG, (creditProfile != null) ? creditProfile.toString() : "");
      }
  }
   private void validateUpdateRequest(TelusCreditProfile creditProfile) {
	     boolean isValid = 
	     		!(
	     		Objects.isNull(creditProfile)
	             );

	     if (!isValid) {
	         LOGGER.warn("{} Invalid creditProfile {}", DATAVALIDATION100, creditProfile);
	         throw new CreditException(HttpStatus.BAD_REQUEST, ERR_CODE_1000, "Request validation failed", ERR_CODE_1000_MSG , (creditProfile!=null)?creditProfile.toString():"");
	     }

	 }    
   
   @Autowired
   private Environment environment;
@RequestMapping(value = "/version")
   @ApiOperation(value = "Provides the current version info of the api.", response = String.class, notes = "Credit Profile API version info.")
   @ApiImplicitParams(value = {
         @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_ACCEPT_LANG, required = false, value = "Language preference.", allowableValues = "en, fr", defaultValue = "en", example = "en", paramType = "header"),
         @ApiImplicitParam(name = CreditMgmtCommonConstants.HEADER_CORR_ID, required = false, value = "Correlation Id, which is a UUID. Used for tracing.", example = "2e15baa3-d272-11e7-a479-05de8af2b6bd", paramType = "header") })
   @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = String.class),
         @ApiResponse(code = 400, message = "Bad Request.", response = ErrorResponse.class),
         @ApiResponse(code = 500, message = "Unknown exception occurred.", response = ErrorResponse.class) })
   public ResponseEntity<String> getVersionInfo() {
	
	//Rate limit how many HTTP requests can be made in a given period of 1 minute
	//limit is  exceeded
	    if (!bucket.tryConsume(1)) {
	    	return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
	    }	
	
	
	  //return profile.getVersionInfo();
	  Properties prop = new Properties();
	  String s;
	  s ="creditmgmt-writeapi(ph2.2):";
	  String[] activeProfiles = this.environment.getActiveProfiles();
		 s = s +"[";
		 s = s + "ActiveProfile: " + Arrays.toString(activeProfiles);
		 s = s +"]"; 
	  try {   	
	     prop.load(CreditProfileApplication.class.getClassLoader().getResourceAsStream("git.properties"));
		 s = s +"[";
		 s = s + "Git information: " + prop;
		 s = s +"]";      
	  } catch (Throwable e) {
		  LOGGER.error("{} : getVersionInfo failed. {}", 
			  ExceptionConstants.STACKDRIVER_METRIC,
			  ExceptionHelper.getStackTrace(e));	  
		  s = "getVersionInfo failed. errormessage="+ e.getMessage();
	  }
	  
	  try { 
		 s = s +"[";
		 s = s + "spring.jdbc.getParameterType.ignore: " + env.getProperty("spring.jdbc.getParameterType.ignore");;
		 s = s +"]"; 
	  } catch (Throwable e) { 		  
		  s = "failed to get spring.jdbc.getParameterType.ignore"+ e.getMessage();
	  }
	  ResponseEntity<String> response = new ResponseEntity<>(s, HttpStatus.OK);
	  return response;

   }

}
