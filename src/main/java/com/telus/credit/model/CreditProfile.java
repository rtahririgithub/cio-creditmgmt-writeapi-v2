package com.telus.credit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.CreditMgmtCommonConstants;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.common.serializer.CustomDecimalSerializer;
import com.telus.credit.common.serializer.StringToLongSerializer;
import com.telus.credit.model.common.ApplicationConstants;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.validation.UniqueIdentification;
import com.telus.credit.validation.ValidCreditClass;
import com.telus.credit.validation.ValidCreditProgramName;
import com.telus.credit.validation.ValidDecisionCode;
import com.telus.credit.validation.ValidLineOfBusiness;
import com.telus.credit.validation.ValidNumber;
import com.telus.credit.validation.ValidProvinceCode;
import com.telus.credit.validation.ValidTimePeriod;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;


@JsonInclude(Include.NON_NULL)

public class CreditProfile {

   private String id;
   private String href;
   
   public String getId() {
	      return id;
	   }
    public void setId(String id) {
      this.id = id;
      this.idDirty = true;
      
      this.setHref(CreditMgmtCommonConstants.creditprofile_GET_HREF+id);
     
   }
	   
	    
   
   @JsonProperty("creationTs")
   private String creationTs;

   @JsonSerialize(using = StringToLongSerializer.class)
   @ValidNumber(min = 0, groups = {Create.class, Patch.class}, message = "1124")
   @JsonProperty("creditRiskLevelNum")
   private String creditRiskLevelNum;

   @JsonSerialize(using = StringToLongSerializer.class)
  // @ValidNumber(groups = {Create.class, Patch.class}, message = "Invalid number")
   @JsonProperty("primaryCreditScoreCd")
   private String primaryCreditScoreCd;

   @ValidTimePeriod(groups = {Create.class, Patch.class})
   private TimePeriod validFor;
   @JsonIgnore
   private boolean idDirty = false;
   @JsonIgnore
   private boolean creditProfileDateDirty = false;
   @JsonIgnore
   private boolean creditRiskRatingDirty = false;
   @JsonIgnore
   private boolean creditScoreDirty = false;
   @JsonIgnore
   private boolean validForDirty = false;
   
   @JsonProperty("creditCheckConsentCd")
   private String creditProfileConsentCd;
 //removed ValidProvinceCode to allow free text value
   //@ValidProvinceCode(groups = {Create.class, Patch.class})
   private String applicationProvinceCd;
   @JsonIgnore
   private boolean applicationProvinceCdDirty = false;
   
  // @ValidLineOfBusiness(groups = {Create.class, Patch.class})
   private String lineOfBusiness;
   @JsonIgnore
   private boolean lineOfBusinessPatch = false;
   
   @JsonProperty("creditProfileLegacyId")   
   @JsonInclude
   private Long creditProfileLegacyId;
   
   
   private String primaryCreditScoreTypeCd;

   @JsonProperty("bureauDecisionCd")
   //@ValidDecisionCode(groups = {Create.class, Patch.class})
   private String bureauDecisionCode;
   @JsonProperty("bureauDecisionCdTxtEn")
   private String bureauDecisionMessage;
   @JsonProperty("bureauDecisionCdTxtFr")
   private String bureauDecisionMessage_fr;
   
   //NotNullcommented out to support wln customer that don't have creditprogramName
   //@NotNull(groups = {Create.class}, message = "1114")
   @ValidCreditProgramName(groups = {Create.class, Patch.class})
   private String creditProgramName;

   @NotNull(groups = {Create.class}, message = "1115")
   @ValidCreditClass(groups = {Create.class, Patch.class}, message = "1115")
   private String creditClassCd;
   
   @JsonProperty("creditClassTs")
   private String creditClassDate;

   private String creditDecisionCd;
   @JsonProperty("creditDecisionTs")
   private String creditDecisionDate;
   @JsonProperty("creditRiskLevelDecisionCd")
   private String riskLevelDecisionCd;
   @JsonProperty("creditRiskLevelTs")
   private String riskLevelDt;

   @JsonSerialize(using = CustomDecimalSerializer.class)
   private BigDecimal clpRatePlanAmt;
   @JsonProperty("clpContractTermNum")
   private Integer clpContractTerm;

   @JsonSerialize(using = CustomDecimalSerializer.class)
  
   @JsonProperty("clpCreditLimitAmt")
   private BigDecimal clpCreditLimitAmt;
   
   @JsonSerialize(using = CustomDecimalSerializer.class)
   private BigDecimal averageSecurityDepositAmt;


   @JsonIgnore
   private boolean riskLevelRiskAssessmentDirty = false;   
   private RiskLevelRiskAssessment riskLevelRiskAssessment;

   @Valid
   @JsonProperty("warnings")
   private List<TelusCreditDecisionWarning> warningHistoryList;


   @Valid 
   @JsonProperty("relatedParty") // relatedParty here is linked to the customer id and doesn't have to do anything with the party table
   private List<RelatedParty> relatedParties;

   private List<Attachments> attachments;


   @JsonProperty("channel")
   private TelusChannel channel;
   @JsonIgnore
   private boolean channelDirty = false;
   
   private String statusCd;
   @JsonIgnore
   boolean statusCdDirty=false;
   
   private String statusTs;  
   @JsonIgnore
   private boolean statusDirty = false;   
   
   private String customerCreditProfileRelCd;
   private Long creditAssessmentId;

   //The properties would be removed on discussion
   @JsonProperty("createdBy")
   private String createdBy;

   @JsonProperty("createdTs")
   private String createdTs;

   @Valid
   @UniqueIdentification(groups = {Create.class, Patch.class})
   @JsonProperty("characteristic")
   private List<TelusCharacteristic> characteristic;


   private Boolean boltonInd;

   @Valid
   @UniqueIdentification(groups = {Create.class, Patch.class})
   private List<ProductCategoryQualification> productCategoryQualification;

   private String busLastUpdtTs;

   @JsonIgnore
   private boolean creditProfileLegacyIdDirty = false;
   @JsonIgnore
   private boolean primaryCreditScoreCdDirty = false;
   @JsonIgnore
   private boolean primaryCreditScoreTypeCdDirty = false;
   @JsonIgnore
   private boolean bureauDecisionCodeDirty = false;
   @JsonIgnore
   private boolean bureauDecisionMessageDirty = false;
   @JsonIgnore
   private boolean creditProgramNameDirty = false;
   @JsonIgnore
   private boolean creditClassCdDirty = false;
   @JsonIgnore
   private boolean creditClassDateDirty = false;
   @JsonIgnore
   private boolean creditDecisionCdDirty = false;
   @JsonIgnore
   private boolean creditDecisionDateDirty = false;
   @JsonIgnore
   private boolean riskLevelNumberDirty = false;
   @JsonIgnore
   private boolean riskLevelDecisionCdDirty = false;
   @JsonIgnore
   private boolean riskLevelDtDirty = false;
   @JsonIgnore
   private boolean clpRatePlanAmtDirty;
   @JsonIgnore
   private boolean clpContractTermDirty;
   @JsonIgnore
   private boolean clpCreditLimitDirty = false;
   @JsonIgnore
   private boolean averageSecurityDepositAmtDirty = false;
   @JsonIgnore
   private boolean assessmentMessageCodeDirty = false;
   @JsonIgnore
   private boolean assessmentMessageDirty = false;
   @JsonIgnore
   private boolean creditAssessmentIdDirty = false;
   @JsonIgnore
   private boolean warningHistoryListDirty = false;
   @JsonIgnore
   private boolean characteristicsDirty = false;
   @JsonIgnore
   private boolean boltonIndDirty;
   @JsonIgnore
   private boolean productCategoryQualificationDirty;

   @JsonIgnore
   private boolean busLastUpdtTsDirty;

   private boolean creditProfileConsentCdDirty=false;

   @ApiModelProperty(value = "The date the profile was established", required = true, example = "2021-01-09T17:01:22.620Z")
   public String getCreationTs() {
      return creationTs;
   }

   public void setCreationTs(String creditProfileDate) {
      this.creationTs = creditProfileDate;
      this.creditProfileDateDirty = true;
   }
   public String getCreditRiskLevelNum() {
      return creditRiskLevelNum;
   }
   public void setCreditRiskLevelNum(String creditRiskRating) {
      this.creditRiskLevelNum = creditRiskRating;
      this.creditRiskRatingDirty = true;
   }

   public String getPrimaryCreditScoreCd() {
      return primaryCreditScoreCd;
   }
   public void setPrimaryCreditScoreCd(String creditScore) {
      this.primaryCreditScoreCd = creditScore;
      this.creditScoreDirty = true;
   }
   public TimePeriod getValidFor() {
      return validFor;
   }
   public void setValidFor(TimePeriod validFor) {
      this.validFor = validFor;
      this.validForDirty = true;
   }

   public String getCreditProfileConsentCd() {
      return creditProfileConsentCd;
   }
   public void setCreditProfileConsentCd(String creditProfileConsentCd) {
      this.creditProfileConsentCd = creditProfileConsentCd;
      this.creditProfileConsentCdDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditProfileConsentCdPatch() {
      return PatchField.patchOrNull(creditProfileConsentCdDirty, creditProfileConsentCd);
   }
   
   public String getApplicationProvinceCd() {
      return applicationProvinceCd;
   }
   public void setApplicationProvinceCd(String applicationProvinceCd) {
      this.applicationProvinceCd = applicationProvinceCd;
      this.applicationProvinceCdDirty = true;
   }
   
   @JsonIgnore
   @Exclude
   public PatchField<String> getApplicationProvinceCdPatch() {
      return PatchField.patchOrNull(applicationProvinceCdDirty, applicationProvinceCd);
   }   
   public String getLineOfBusiness() {
      return lineOfBusiness;
   }
   public void setLineOfBusiness(String lineOfBusiness) {
      this.lineOfBusiness = lineOfBusiness;
      this.lineOfBusinessPatch=true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<String> getLineOfBusinessPatch() {
      return PatchField.patchOrNull(lineOfBusinessPatch, lineOfBusiness);
   }
   
   @JsonProperty("@baseType")
   @Exclude
   public String getBaseType() {
      return  ApplicationConstants.PROFILE_BASE_TYPE;//StringUtils.defaultIfBlank(baseType, ApplicationConstants.PROFILE_BASE_TYPE);
   }

   @Exclude
   public void setBaseType(String baseType) {
   }

   @JsonProperty("@type")
   @Exclude
   public String getType() {
      return ApplicationConstants.PROFILE_TYPE;//StringUtils.defaultIfBlank(type, ApplicationConstants.PROFILE_TYPE);
   }

   @Exclude
   public void setType(String type) {
   }

   public String getPrimaryCreditScoreTypeCd() {
      return primaryCreditScoreTypeCd;
   }
   public void setPrimaryCreditScoreTypeCd(String primaryCreditScoreTypeCd) {
      this.primaryCreditScoreTypeCd = primaryCreditScoreTypeCd;
      this.primaryCreditScoreTypeCdDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<String> getPrimaryCreditScoreTypeCdPatch() {
      return PatchField.patchOrNull(primaryCreditScoreTypeCdDirty, primaryCreditScoreTypeCd);
   }
   public String getBureauDecisionCode() {
      return bureauDecisionCode;
   }
   public void setBureauDecisionCode(String bureauDecisionCode) {
      this.bureauDecisionCode = bureauDecisionCode;
      this.bureauDecisionCodeDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<String> getBureauDecisionCodePatch() {
      return PatchField.patchOrNull(bureauDecisionCodeDirty, bureauDecisionCode);
   }
   
   @Exclude
   public String getBureauDecisionMessage() {
      return bureauDecisionMessage;
   }
   @Exclude
   public void setBureauDecisionMessage(String bureauDecisionMessage) {
      this.bureauDecisionMessage = bureauDecisionMessage;
      this.bureauDecisionMessageDirty = true;
   }

   @Exclude
   public String getBureauDecisionMessage_fr() {
      return bureauDecisionMessage_fr;
   }
   @Exclude
   public void setBureauDecisionMessage_fr(String bureauDecisionMessage_fr) {
      this.bureauDecisionMessage_fr = bureauDecisionMessage_fr;
   }

   public String getCreditProgramName() {
      return creditProgramName;
   }
   public void setCreditProgramName(String creditProgramName) {
      this.creditProgramName = creditProgramName;
      this.creditProgramNameDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditProgramNamePatch() {
      return PatchField.patchOrNull(creditProgramNameDirty, creditProgramName);
   }   
   public String getCreditClassCd() {
      return creditClassCd;
   }
   public void setCreditClassCd(String creditClassCd) {
      this.creditClassCd = creditClassCd;
      this.creditClassCdDirty = true;
   }
   public String getCreditClassDate() {
      return creditClassDate;
   }
   public void setCreditClassDate(String creditClassDate) {
      this.creditClassDate = creditClassDate;
      this.creditClassDateDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditClassDatePatch() {
      return PatchField.patchOrNull(creditClassDateDirty, creditClassDate);
   }
   
   public String getCreditDecisionCd() {
      return creditDecisionCd;
   }
   public void setCreditDecisionCd(String creditDecisionCd) {
      this.creditDecisionCd = creditDecisionCd;
      this.creditDecisionCdDirty = true;
   }
   
   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditDecisionCdPatch() {
      return PatchField.patchOrNull(creditDecisionCdDirty, creditDecisionCd);
   }
   
   public String getCreditDecisionDate() {
      return creditDecisionDate;
   }
   public void setCreditDecisionDate(String creditDecisionDate) {
      this.creditDecisionDate = creditDecisionDate;
      this.creditDecisionDateDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditDecisionDatePatch() {
      return PatchField.patchOrNull(creditDecisionDateDirty, creditDecisionDate);
   }
   
   public String getRiskLevelDecisionCd() {
      return riskLevelDecisionCd;
   }
   public void setRiskLevelDecisionCd(String riskLevelDecisionCd) {
      this.riskLevelDecisionCd = riskLevelDecisionCd;
      this.riskLevelDecisionCdDirty = true;
   }
   
   @JsonIgnore
   @Exclude
   public PatchField<String> getRiskLevelDecisionCdPatch() {
      return PatchField.patchOrNull(riskLevelDtDirty, riskLevelDecisionCd);
   }
   
   public String getRiskLevelDt() {
      return riskLevelDt;
   }
   public void setRiskLevelDt(String riskLevelDt) {
      this.riskLevelDt = riskLevelDt;
      this.riskLevelDtDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<String> getRiskLevelDtPatch() {
      return PatchField.patchOrNull(riskLevelDtDirty, riskLevelDt);
   }
   
   public BigDecimal getClpRatePlanAmt() {
      return clpRatePlanAmt;
   }
   public void setClpRatePlanAmt(BigDecimal clpRatePlanAmt) {
      this.clpRatePlanAmt = clpRatePlanAmt;
      this.clpRatePlanAmtDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<BigDecimal> getClpRatePlanAmtPatch() {
      return PatchField.patchOrNull(clpRatePlanAmtDirty, clpRatePlanAmt);
   }
   
   public Integer getClpContractTerm() {
      return clpContractTerm;
   }
   
   public void setClpContractTerm(Integer clpContractTerm) {
      this.clpContractTerm = clpContractTerm;
      this.clpContractTermDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<Integer> getClpContractTermPatch() {
      return PatchField.patchOrNull(clpContractTermDirty, clpContractTerm);
   }

   
   public String getBusLastUpdtTs() {
      return busLastUpdtTs;
   }

   public void setBusLastUpdtTs(String busLastUpdtTs) {
      this.busLastUpdtTs = busLastUpdtTs;
      this.busLastUpdtTsDirty = true;
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getBusLastUpdtTsPatch() {
      return PatchField.patchOrNull(busLastUpdtTsDirty, busLastUpdtTs);
   }


	
   public BigDecimal getClpCreditLimitAmt() {
      return clpCreditLimitAmt;
   }
   public void setClpCreditLimitAmt(BigDecimal clpCreditLimit) {
      this.clpCreditLimitAmt = clpCreditLimit;
      this.clpCreditLimitDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<BigDecimal> getClpCreditLimitAmtPatch() {
      return PatchField.patchOrNull(clpCreditLimitDirty, clpCreditLimitAmt);
   }
   
   //   public String getAverageSecurityDepositListProductCd() {
//      return averageSecurityDepositListProductCd;
//   }
//   public void setAverageSecurityDepositListProductCd(String averageSecurityDepositListProductCd) {
//      this.averageSecurityDepositListProductCd = averageSecurityDepositListProductCd;
//      this.averageSecurityDepositListProductCd", this.averageSecurityDepositListProductCd);
//   }
   public BigDecimal getAverageSecurityDepositAmt() {
      return averageSecurityDepositAmt;
   }
   public void setAverageSecurityDepositAmt(BigDecimal averageSecurityDepositAmt) {
      this.averageSecurityDepositAmt = averageSecurityDepositAmt;
      this.averageSecurityDepositAmtDirty = true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<BigDecimal> getAverageSecurityDepositAmtPatch() {
      return PatchField.patchOrNull(averageSecurityDepositAmtDirty, averageSecurityDepositAmt);
   }
   public List<TelusCreditDecisionWarning> getWarningHistoryList() {
      return warningHistoryList;
   }
   public void setWarningHistoryList(List<TelusCreditDecisionWarning> warningHistoryList) {
      this.warningHistoryList = warningHistoryList;
      this.warningHistoryListDirty = true;
   }

   public List<RelatedParty> getRelatedParties() {
      return relatedParties;
   }

   public void setRelatedParties(List<RelatedParty> relatedParties) {
      this.relatedParties = relatedParties;
   }

   public List<Attachments> getAttachments() {
      return attachments;
   }

   public void setAttachments(List<Attachments> attachments) {
      this.attachments = attachments;
   }

	// @JsonIgnore commented to support Wireline requirement to return creditProfileLegacyId to soap svc consumers.
	 public Long getCreditProfileLegacyId() { 
		 return creditProfileLegacyId; 
	 } 
	 @JsonIgnore	  
	 @Exclude public PatchField<Long> getCreditProfileLegacyIdPatch() { 
		 return PatchField.patchOrNull(creditProfileLegacyIdDirty, creditProfileLegacyId); 
	 }	 
	 public void setCreditProfileLegacyId(Long creditProfileLegacyId) { 
		 this.creditProfileLegacyId = creditProfileLegacyId;
		 this.creditProfileLegacyIdDirty = true; 
	}
	 

   public String getStatusCd(){
      return statusCd;
   }

   public void setStatusCd(String creditProfileStatusCd) {
      this.statusCd = creditProfileStatusCd;
      this.statusCdDirty =true;       
   }
   @JsonIgnore
   @Exclude
   public PatchField<String> getStatusCdPatch() {
      return PatchField.patchOrNull(statusCdDirty, statusCd);
   }
   
   public String getStatusTs() {
      return statusTs;
   }

   public void setStatusTs(String creditProfileStatusTs) {
      this.statusTs = creditProfileStatusTs;
      this.statusDirty =true;
   }
   
   @JsonIgnore
   @Exclude
   public PatchField<String> getStatusTsPatch() {
      return PatchField.patchOrNull(statusDirty, statusTs);
   }

   
   public String getCustomerCreditProfileRelCd() {
      return customerCreditProfileRelCd;
   }

   public void setCustomerCreditProfileRelCd(String customerCreditProfileRelCd) {
      this.customerCreditProfileRelCd = customerCreditProfileRelCd;
   }

   public RiskLevelRiskAssessment getRiskLevelRiskAssessment() {
      return riskLevelRiskAssessment;
   }

   public void setRiskLevelRiskAssessment(RiskLevelRiskAssessment riskLevelRiskAssessment) {
      this.riskLevelRiskAssessment = riskLevelRiskAssessment;
      this.riskLevelRiskAssessmentDirty=true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<RiskLevelRiskAssessment> getRiskLevelRiskAssessmentPatch() {
      return PatchField.patchOrNull(riskLevelRiskAssessmentDirty, riskLevelRiskAssessment);
   }
   
   public Long getCreditAssessmentId() {
      return creditAssessmentId;
   }

   public void setCreditAssessmentId(Long creditAssessmentId) {
      this.creditAssessmentId = creditAssessmentId;
   }

   public String getCreatedBy() {
      return createdBy;
   }

   public void setCreatedBy(String createdBy) {
      this.createdBy = createdBy;
   }

   public String getCreatedTs() {
      return createdTs;
   }

   public void setCreatedTs(String createdTs) {
      this.createdTs = createdTs;
   }

   public TelusChannel getChannel() {
      return channel;
   }

   public void setChannel(TelusChannel channel) {
      this.channel = channel;
      this.channelDirty=true;
   }
   @JsonIgnore
   @Exclude
   public PatchField<TelusChannel> getChannelPatch() {
      return PatchField.patchOrNull(channelDirty, channel);
   }
   
   @JsonIgnore
   @Exclude
   public PatchField<String> getIdPatch() {
      return PatchField.patchOrNull(idDirty, id);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditProfileDatePatch() {
      return PatchField.patchOrNull(creditProfileDateDirty, creationTs);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditRiskRatingPatch() {
      return PatchField.patchOrNull(creditRiskRatingDirty, creditRiskLevelNum);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditScorePatch() {
      return PatchField.patchOrNull(creditScoreDirty, primaryCreditScoreCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<TimePeriod> getValidForPatch() {
      return PatchField.patchOrNull(validForDirty, validFor);
   }

   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
   
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}

   public List<TelusCharacteristic> getCharacteristic() {
      return characteristic;
   }

   public void setCharacteristic(List<TelusCharacteristic> characteristiclist) {
      this.characteristic = characteristiclist;
      this.characteristicsDirty = true;
   }

   @JsonIgnore
   @Exclude
   public PatchField<List<TelusCharacteristic>> getCharacteristicsPatch() {
      return PatchField.patchOrNull(characteristicsDirty, characteristic);
   }

   @JsonIgnore
   @Exclude
   private String updatedTs;
	public String getUpdatedTs() {
		if(updatedTs==null ) {
			return createdTs;
		}
		return updatedTs;
	}
	public void setUpdatedTs(String updatedTs) {
		this.updatedTs = updatedTs;
	}


   public Boolean getBoltonInd() {
      return boltonInd;
   }
   @JsonIgnore
   @Exclude
   public PatchField<Boolean> getBoltonIndPatch() {
      return PatchField.patchOrNull(boltonIndDirty, boltonInd);
   }

public void setBoltonInd(Boolean boltonInd) {
      this.boltonInd = boltonInd;
      this.boltonIndDirty = true;
   }

   @JsonIgnore
   @Exclude
   public PatchField<List<ProductCategoryQualification>> getProductCategoryQualificationPatch() {
      return PatchField.patchOrNull(productCategoryQualificationDirty, productCategoryQualification);
   }

   public List<ProductCategoryQualification> getProductCategoryQualification() {
      return productCategoryQualification;
   }

   public void setProductCategoryQualification(List<ProductCategoryQualification> productCategoryQualification) {
      this.productCategoryQualification = productCategoryQualification;
      this.productCategoryQualificationDirty = true;
   }

   
   @JsonIgnore
   @Exclude   
   public PatchField<String> getCreditClassCdPatch() {
      return PatchField.patchOrNull(creditClassCdDirty, creditClassCd);
   }

}
