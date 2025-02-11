
package com.telus.credit.pds.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Ref PDS JSON Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "addressType",
    "cdaBillingAccountType",
    "countryOverseas",
    "creditClass",
    "jurisdictionType",
    "provinceState",
    "assessmentMessage",
    "creditDecisionRule",
    "creditOperationParameter",
    "creditProgramName",
    "creditWarningCategory"
})
public class ReferenceData {

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("addressType")
    @JsonPropertyDescription("Reference data items array.")
    private List<SingleKeyReferenceDataItem> addressType = null;
    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("cdaBillingAccountType")
    @JsonPropertyDescription("Reference data items array.")
    private List<SingleKeyReferenceDataItem> cdaBillingAccountType = null;
    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("countryOverseas")
    @JsonPropertyDescription("Reference data items array.")
    private List<SingleKeyReferenceDataItem> countryOverseas = null;
    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("creditClass")
    @JsonPropertyDescription("Reference data items array.")
    private List<SingleKeyReferenceDataItem> creditClass = null;
    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("jurisdictionType")
    @JsonPropertyDescription("Reference data items array.")
    private List<SingleKeyReferenceDataItem> jurisdictionType = null;
    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("provinceState")
    @JsonPropertyDescription("Reference data items array.")
    private List<SingleKeyReferenceDataItem> provinceState = null;
    /**
     * Reference data items array.
     *
     */
    @JsonProperty("assessmentMessage")
    @JsonPropertyDescription("Reference data items array.")
    private List<MultiKeyReferenceDataItem> assessmentMessage = null;
    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("creditDecisionRule")
    @JsonPropertyDescription("Reference data items array.")
    private List<MultiKeyReferenceDataItem> creditDecisionRule = null;
    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("creditOperationParameter")
    @JsonPropertyDescription("Reference data items array.")
    private List<MultiKeyReferenceDataItem> creditOperationParameter = null;
    /**
     * Reference data items array.
     *
     */
    @JsonProperty("creditProgramName")
    @JsonPropertyDescription("Reference data items array.")
    private List<SingleKeyReferenceDataItem> creditProgramName = null;
    /**
     * Reference data items array.
     *
     */
    @JsonProperty("creditWarningCategory")
    @JsonPropertyDescription("Reference data items array.")
    private List<SingleKeyReferenceDataItem> creditWarningCategory = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("addressType")
    public List<SingleKeyReferenceDataItem> getAddressType() {
        return addressType;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("addressType")
    public void setAddressType(List<SingleKeyReferenceDataItem> addressType) {
        this.addressType = addressType;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("cdaBillingAccountType")
    public List<SingleKeyReferenceDataItem> getCdaBillingAccountType() {
        return cdaBillingAccountType;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("cdaBillingAccountType")
    public void setCdaBillingAccountType(List<SingleKeyReferenceDataItem> cdaBillingAccountType) {
        this.cdaBillingAccountType = cdaBillingAccountType;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("countryOverseas")
    public List<SingleKeyReferenceDataItem> getCountryOverseas() {
        return countryOverseas;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("countryOverseas")
    public void setCountryOverseas(List<SingleKeyReferenceDataItem> countryOverseas) {
        this.countryOverseas = countryOverseas;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("creditClass")
    public List<SingleKeyReferenceDataItem> getCreditClass() {
        return creditClass;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("creditClass")
    public void setCreditClass(List<SingleKeyReferenceDataItem> creditClass) {
        this.creditClass = creditClass;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("jurisdictionType")
    public List<SingleKeyReferenceDataItem> getJurisdictionType() {
        return jurisdictionType;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("jurisdictionType")
    public void setJurisdictionType(List<SingleKeyReferenceDataItem> jurisdictionType) {
        this.jurisdictionType = jurisdictionType;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("provinceState")
    public List<SingleKeyReferenceDataItem> getProvinceState() {
        return provinceState;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("provinceState")
    public void setProvinceState(List<SingleKeyReferenceDataItem> provinceState) {
        this.provinceState = provinceState;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("creditDecisionRule")
    public List<MultiKeyReferenceDataItem> getCreditDecisionRule() {
        return creditDecisionRule;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("creditDecisionRule")
    public void setCreditDecisionRule(List<MultiKeyReferenceDataItem> creditDecisionRule) {
        this.creditDecisionRule = creditDecisionRule;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("creditOperationParameter")
    public List<MultiKeyReferenceDataItem> getCreditOperationParameter() {
        return creditOperationParameter;
    }

    /**
     * Reference data items array.
     * 
     */
    @JsonProperty("creditOperationParameter")
    public void setCreditOperationParameter(List<MultiKeyReferenceDataItem> creditOperationParameter) {
        this.creditOperationParameter = creditOperationParameter;
    }

    /**
     * Reference data items array.
     *
     */
    @JsonProperty("assessmentMessage")
    public List<MultiKeyReferenceDataItem> getAssessmentMessage() {
        return assessmentMessage;
    }

    /**
     * Reference data items array.
     *
     */
    @JsonProperty("assessmentMessage")
    public void setAssessmentMessage(List<MultiKeyReferenceDataItem> assessmentMessage) {
        this.assessmentMessage = assessmentMessage;
    }
    /**
     * Reference data items array.
     *
     */
    @JsonProperty("creditProgramName")
    public List<SingleKeyReferenceDataItem> getCreditProgramName() {
        return creditProgramName;
    }
    /**
     * Reference data items array.
     *
     */
    @JsonProperty("creditProgramName")
    public void setCreditProgramName(List<SingleKeyReferenceDataItem> creditProgramName) {
        this.creditProgramName = creditProgramName;
    }
    /**
     * Reference data items array.
     *
     */
    @JsonProperty("creditWarningCategory")
    public List<SingleKeyReferenceDataItem> getCreditWarningCategory() {
        return creditWarningCategory;
    }
    /**
     * Reference data items array.
     *
     */
    @JsonProperty("creditWarningCategory")
    public void setCreditWarningCategory(List<SingleKeyReferenceDataItem> creditWarningCategory) {
        this.creditWarningCategory = creditWarningCategory;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
