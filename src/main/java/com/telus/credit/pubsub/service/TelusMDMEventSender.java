package com.telus.credit.pubsub.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.telus.credit.config.CreditPubSubConfig;
import com.telus.credit.config.CreditWorthinessConfig;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.model.CreditProfile;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCharacteristic;
import com.telus.credit.model.TelusIndividualIdentification;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.telus.credit.common.MDMEventConstants.ALTIDTYPE;
import static com.telus.credit.common.MDMEventConstants.ALTIDVALUE;
import static com.telus.credit.common.MDMEventConstants.COUNTRY;
import static com.telus.credit.common.MDMEventConstants.CREATE;
import static com.telus.credit.common.MDMEventConstants.CREATEDATE;
import static com.telus.credit.common.MDMEventConstants.CREATOR;
import static com.telus.credit.common.MDMEventConstants.CUSTOMERID;
import static com.telus.credit.common.MDMEventConstants.CUST_CREDIT_DETAILS;
import static com.telus.credit.common.MDMEventConstants.CUST_CREDIT_IDENTIFIER;
import static com.telus.credit.common.MDMEventConstants.DOB;
import static com.telus.credit.common.MDMEventConstants.DRIVERS_LICENSE_KEY;
import static com.telus.credit.common.MDMEventConstants.HEALTH_CARE_NUMBER_KEY;
import static com.telus.credit.common.MDMEventConstants.ITEMS;
import static com.telus.credit.common.MDMEventConstants.PASSPORT_NUMBER_KEY;
import static com.telus.credit.common.MDMEventConstants.PROVINCE;
import static com.telus.credit.common.MDMEventConstants.PROVINCIAL_ID_KEY;
import static com.telus.credit.common.MDMEventConstants.SIN_KEY;
import static com.telus.credit.common.MDMEventConstants.SOURCEKEY;
import static com.telus.credit.common.MDMEventConstants.UPDATEBY;
import static com.telus.credit.common.MDMEventConstants.UPDATEDATE;
import static com.telus.credit.common.MDMEventConstants.UPDATEDDOB;

@Service
public class TelusMDMEventSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelusMDMEventSender.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final CreditPubSubConfig creditPubSubConfig;
    private final RestTemplate restTemplate;
    private final CreditWorthinessConfig creditWorthinessConfig;
    public TelusMDMEventSender(CreditPubSubConfig creditPubSubConfig, RestTemplate restTemplate, CreditWorthinessConfig creditWorthinessConfig) {
        this.creditPubSubConfig = creditPubSubConfig;
        this.restTemplate = restTemplate;
        this.creditWorthinessConfig = creditWorthinessConfig;
    }

    public String publish(String message) {
        String result = null;
        if (Objects.isNull(message)) {
            return result;
        }
        HttpEntity<String> request=null;
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
            httpHeaders.setBasicAuth(HttpHeaders.encodeBasicAuth(creditPubSubConfig.getUsername(), creditPubSubConfig.getPassword(), StandardCharsets.UTF_8));
            request = new HttpEntity<>(message, httpHeaders);

            LOGGER.info("publishing MDM TelusCreditProfilePubSubEvent Request. URL = {} ,  message = {}",  creditPubSubConfig.getPubSubURL(), request);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(creditPubSubConfig.getPubSubURL(), request, String.class);
            LOGGER.info("publishing MDM TelusCreditProfilePubSubEvent Response Status {}, message {}", responseEntity.getStatusCode(), responseEntity.getBody());

            result = responseEntity.getBody();
        } catch (Exception e) {
            LOGGER.error("{} Error publishing MDM  TelusCreditProfilePubSubEvent. URL = {} , message ={} , StackTrace ={}",
            		ExceptionConstants.STACKDRIVER_METRIC,
            		creditPubSubConfig.getPubSubURL(),
            		request,
            		ExceptionHelper.getStackTrace(e)
            		);
        }
        return result;
    }

    public String createJSONMessageForCreditIds(List<TelusIndividualIdentification> telusIndividualIdentifications, String customerId, String userID, String typeCode) {

        ObjectNode customerPersonIdentifiers = MAPPER.createObjectNode();
        try {
            if (!CollectionUtils.isEmpty(telusIndividualIdentifications)) {
                telusIndividualIdentifications.stream()
                        .filter(telusIndividualIdentification -> {
                            try {
                            	String identificationId = (telusIndividualIdentification!=null)?telusIndividualIdentification.getIdentificationId():"0";
                                int value = Integer.parseInt(identificationId);
                                return value != 0;
                            } catch (NumberFormatException ignored) {
                            }
                            return true;
                        })
                        .forEach(telusIndividualIdentification -> {
                            try {
                                customerPersonIdentifiers.put(CREATE.equals(typeCode) ? CREATEDATE : UPDATEDATE, String.valueOf(Calendar.getInstance().getTime()));
                                customerPersonIdentifiers.put(CREATE.equals(typeCode) ? CREATOR : UPDATEBY, userID);
                                customerPersonIdentifiers.put(CREATE.equals(typeCode) ? CREATOR : UPDATEBY, userID);
                                ArrayNode items = MAPPER.createArrayNode();
                                String identificationType = (telusIndividualIdentification!=null)?telusIndividualIdentification.getIdentificationType():null;
                                if(identificationType!=null) {
	                                if (SIN_KEY.equalsIgnoreCase(identificationType)) {
	                                    ObjectNode sin = MAPPER.createObjectNode();
	                                    sin.put(ALTIDVALUE, telusIndividualIdentification.getIdentificationId());
	                                    sin.put(ALTIDTYPE, SIN_KEY);
	                                    sin.put(SOURCEKEY, customerId + "_" + SIN_KEY);
	                                    items.add(sin);
	
	                                } else if (DRIVERS_LICENSE_KEY.equalsIgnoreCase(identificationType)) {
	                                    ObjectNode dl = MAPPER.createObjectNode();
	                                    dl.put(ALTIDVALUE, telusIndividualIdentification.getIdentificationId());
	                                    dl.put(ALTIDTYPE, DRIVERS_LICENSE_KEY);
	                                    String aProvinceCd = (telusIndividualIdentification.getTelusCharacteristic()!=null)?telusIndividualIdentification.getTelusCharacteristic().getProvinceCd():null;
	                                    dl.put(PROVINCE, aProvinceCd);// create
	                                    dl.put(SOURCEKEY, customerId + "_" + DRIVERS_LICENSE_KEY);
	                                    items.add(dl);
	
	                                } else if (HEALTH_CARE_NUMBER_KEY.equalsIgnoreCase(identificationType)) {
	                                    ObjectNode healthCard = MAPPER.createObjectNode();
	                                    healthCard.put(ALTIDVALUE, telusIndividualIdentification.getIdentificationId());
	                                    healthCard.put(ALTIDTYPE, HEALTH_CARE_NUMBER_KEY);
	                                    String aProvinceCd = (telusIndividualIdentification.getTelusCharacteristic()!=null)?telusIndividualIdentification.getTelusCharacteristic().getProvinceCd():null;
	                                    healthCard.put(PROVINCE, aProvinceCd);
	                                    healthCard.put(SOURCEKEY, customerId + "_" + HEALTH_CARE_NUMBER_KEY);
	                                    items.add(healthCard);
	
	                                } else if (PASSPORT_NUMBER_KEY.equalsIgnoreCase(identificationType)) {
	                                    ObjectNode passport = MAPPER.createObjectNode();
	                                    passport.put(ALTIDVALUE, telusIndividualIdentification.getIdentificationId());
	                                    passport.put(ALTIDTYPE, PASSPORT_NUMBER_KEY);
	                                    String aCountryCd = (telusIndividualIdentification.getTelusCharacteristic()!=null)?telusIndividualIdentification.getTelusCharacteristic().getCountryCd():null;
	                                    passport.put(COUNTRY, aCountryCd);
	                                    passport.put(SOURCEKEY, customerId + "_" + PASSPORT_NUMBER_KEY);
	                                    items.add(passport);
	
	                                } else if (PROVINCIAL_ID_KEY.equalsIgnoreCase(identificationType)) {
	                                    ObjectNode provinceCode = MAPPER.createObjectNode();
	                                    provinceCode.put(ALTIDVALUE, telusIndividualIdentification.getIdentificationId());
	                                    provinceCode.put(ALTIDTYPE, PROVINCIAL_ID_KEY);
	                                    String aProvinceCd = (telusIndividualIdentification.getTelusCharacteristic()!=null)?telusIndividualIdentification.getTelusCharacteristic().getProvinceCd():null;
	                                    provinceCode.put(PROVINCE, aProvinceCd);
	                                    provinceCode.put(SOURCEKEY, customerId + "_" + PROVINCIAL_ID_KEY);
	                                    items.add(provinceCode);
	                                }
	                              }
                                if (!items.isEmpty()) {
                                    customerPersonIdentifiers.set(ITEMS, items);
                                }
                            } catch (Exception e) {
                                LOGGER.error("CustId={}. {}:  createJSONMessageForCreditIds. {} ", customerId, ExceptionConstants.STACKDRIVER_METRIC, ExceptionHelper.getStackTrace(e));
                            }
                        });

            }
            ObjectNode customerNode = MAPPER.createObjectNode();
            customerNode.set(CUST_CREDIT_IDENTIFIER, MAPPER.createArrayNode().add(customerPersonIdentifiers));
            customerNode.set(CUSTOMERID, MAPPER.createArrayNode().add(Long.valueOf(customerId)));
            ObjectNode CustomerCreditDetails = MAPPER.createObjectNode();
            CustomerCreditDetails.set(CUST_CREDIT_DETAILS, MAPPER.createArrayNode().add(customerNode));
            LOGGER.debug("createJSONMessageForCreditIds ->CustomerCreditDetails->{}", CustomerCreditDetails);
            return CustomerCreditDetails.toString();
        } catch (Exception e) {
            LOGGER.error("CustId={}. {}:  createJSONMessageForCreditIds. {} ", customerId, ExceptionConstants.STACKDRIVER_METRIC, ExceptionHelper.getStackTrace(e));
        }
        return MAPPER.createObjectNode().toString();
    }

    public boolean isCreditIdWithZeroValue(String creditIdValue) {
        boolean result = false;
        LOGGER.debug("Checking for zeros in = " + creditIdValue);
        if (creditIdValue != null && creditIdValue.length() > 0) {
            String decryptedId = creditIdValue;
            try {
                int decryptedIdValue = Integer.parseInt(decryptedId);
                if (decryptedIdValue == 0) {
                    result = true;
                }
            } catch (NumberFormatException ne) {
            }
        }
        LOGGER.debug("Result = " + result);
        return result;
    }

    public String createJSONMessageForDOB(String customerId, Date dateObirth, String userID) {

        ObjectNode customerNode = MAPPER.createObjectNode();
        try {
            LOGGER.debug("createJSONMessageForDOB: dateObirth ->{}", dateObirth);
            if(dateObirth!=null) {
	            ObjectNode dob = MAPPER.createObjectNode();
	            dob.set(DOB, MAPPER.createArrayNode().add(String.valueOf(dateObirth)));
	            customerNode.set(CUSTOMERID, MAPPER.createArrayNode().add(Integer.valueOf(customerId)));
	            customerNode.set(UPDATEBY, MAPPER.createArrayNode().add(userID));
	            customerNode.set(UPDATEDATE, MAPPER.createArrayNode().add(String.valueOf(Calendar.getInstance().getTime())));
	            customerNode.set(UPDATEDDOB, MAPPER.createArrayNode().add(dob));
            }
            LOGGER.debug("createJSONMessageForDOB ->createJSONMessageForDOB-> {}", customerNode);
        } catch (Exception e) {
            LOGGER.error("CustId={}. {}:  createJSONMessageForDOB. {} ", customerId, ExceptionConstants.STACKDRIVER_METRIC, ExceptionHelper.getStackTrace(e));
        }
        return customerNode.toString();
    }

    public JSONObject performOverrideCreditWorthiness(long customerId, CreditProfile modifiedCreditProfile, TelusChannel telusChannel, String creditAssessmentType) {
        JSONObject overrideCreditWorthiness = new JSONObject();
        try {
            overrideCreditWorthiness.put("creditAssessmentTypeCd", creditAssessmentType);
            if (creditAssessmentType.equals(creditWorthinessConfig.getOverrideCreditAssessmentType())){
                overrideCreditWorthiness.put("newCreditValueCd", modifiedCreditProfile.getCreditClassCd());
                if (!CollectionUtils.isEmpty(modifiedCreditProfile.getWarningHistoryList())) {
                    overrideCreditWorthiness.put("newFraudIndictorCd", modifiedCreditProfile.getWarningHistoryList().get(0).getWarningStatusCd());
                }
                overrideCreditWorthiness.put("creditAssessmentSubTypeCd", creditWorthinessConfig.getManualOverrideSubType());

            } else if (creditAssessmentType.equals(creditWorthinessConfig.getAuditCreditAssessmentType())) {

                overrideCreditWorthiness.put("creditAssessmentSubTypeCd", creditWorthinessConfig.getBureauConsentSubType());
                overrideCreditWorthiness.put("newCreditCheckConsentCd", modifiedCreditProfile.getCreditProfileConsentCd());
            }
            overrideCreditWorthiness.put("applicationID", telusChannel.getOriginatorAppId());
            overrideCreditWorthiness.put("lineOfBusiness", "WIRELINE");
            overrideCreditWorthiness.put("customerID", customerId);
            if (!CollectionUtils.isEmpty(modifiedCreditProfile.getCharacteristic())) {
                Optional<String> commentTxtOptional = modifiedCreditProfile.getCharacteristic()
                        .stream()
                        .filter(characteristic -> characteristic.getName().equals(""))
                        .map(TelusCharacteristic::getValue)
                        .findFirst();
                if (commentTxtOptional.isPresent()) {
                    overrideCreditWorthiness.put("commentTxt", commentTxtOptional.get());
                }
            }
        } catch (Exception e) {
            LOGGER.error("CustId={}. {}:  performOverrideCreditWorthiness. {} ", customerId, ExceptionConstants.STACKDRIVER_METRIC, ExceptionHelper.getStackTrace(e));
        }
        return overrideCreditWorthiness;
    }

}
