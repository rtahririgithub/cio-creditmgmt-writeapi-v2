package com.telus.credit.model;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.common.serializer.CustomDecimalSerializer;
import com.telus.credit.common.serializer.StringToLongSerializer;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.validation.ValidCreditClass;
import com.telus.credit.validation.ValidCreditProgramName;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

@JsonInclude(Include.NON_NULL)
public class TelusCreditProfileCharacteristic { 

	 @JsonAlias("creditProfileIdLegacy")
	 @JsonProperty("creditProfileLegacyId")
     @JsonInclude private Long creditProfileLegacyId;
	 
   
   private String primaryCreditScoreCd;
   private String primaryCreditScoreTypeCd;
   //@ValidDecisionCode(groups = {Create.class, Patch.class})
   private String bureauDecisionCode;
   private String bureauDecisionMessage;
   private String bureauDecisionMessage_fr;
   @ValidCreditProgramName(groups = {Create.class, Patch.class})
   private String creditProgramName;

   @ValidCreditClass(groups = {Create.class, Patch.class})
   private String creditClassCd;

   private String creditClassDate;

   private String creditDecisionCd;

   private String creditDecisionDate;
   @JsonSerialize(using = StringToLongSerializer.class)
   private String riskLevelNumber;

   private String riskLevelDecisionCd;

   private String riskLevelDt;

   @JsonSerialize(using = CustomDecimalSerializer.class)
   private BigDecimal clpRatePlanAmt;

   private Integer clpContractTerm;

   @JsonSerialize(using = CustomDecimalSerializer.class)
   private BigDecimal clpCreditLimit;
   @JsonSerialize(using = CustomDecimalSerializer.class)
   private BigDecimal averageSecurityDepositAmt;
   private String assessmentMessageCode;
   private String assessmentMessage;
   private String assessmentMessage_fr;
   private Long creditAssessmentId;
   @Valid
   private List<TelusCreditDecisionWarning> warningHistoryList;


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

   public String getPrimaryCreditScoreCd() {
      return primaryCreditScoreCd;
   }
   public void setPrimaryCreditScoreCd(String primaryCreditScoreCd) {
      this.primaryCreditScoreCd = primaryCreditScoreCd;
      this.primaryCreditScoreCdDirty = true;
   }
   public String getPrimaryCreditScoreTypeCd() {
      return primaryCreditScoreTypeCd;
   }
   public void setPrimaryCreditScoreTypeCd(String primaryCreditScoreTypeCd) {
      this.primaryCreditScoreTypeCd = primaryCreditScoreTypeCd;
      this.primaryCreditScoreTypeCdDirty = true;
   }
   public String getBureauDecisionCode() {
      return bureauDecisionCode;
   }
   public void setBureauDecisionCode(String bureauDecisionCode) {
      this.bureauDecisionCode = bureauDecisionCode;
      this.bureauDecisionCodeDirty = true;
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
   public String getCreditDecisionCd() {
      return creditDecisionCd;
   }
   public void setCreditDecisionCd(String creditDecisionCd) {
      this.creditDecisionCd = creditDecisionCd;
      this.creditDecisionCdDirty = true;
   }

   public String getCreditDecisionDate() {
      return creditDecisionDate;
   }
   public void setCreditDecisionDate(String creditDecisionDate) {
      this.creditDecisionDate = creditDecisionDate;
      this.creditDecisionDateDirty = true;
   }
   public String getRiskLevelNumber() {
      return riskLevelNumber;
   }
   public void setRiskLevelNumber(String riskLevelNumber) {
      this.riskLevelNumber = riskLevelNumber;
      this.riskLevelNumberDirty = true;
   }

   public String getRiskLevelDecisionCd() {
      return riskLevelDecisionCd;
   }
   public void setRiskLevelDecisionCd(String riskLevelDecisionCd) {
      this.riskLevelDecisionCd = riskLevelDecisionCd;
      this.riskLevelDecisionCdDirty = true;
   }

   public String getRiskLevelDt() {
      return riskLevelDt;
   }
   public void setRiskLevelDt(String riskLevelDt) {
      this.riskLevelDt = riskLevelDt;
      this.riskLevelDtDirty = true;
   }

   public BigDecimal getClpRatePlanAmt() {
      return clpRatePlanAmt;
   }
   public void setClpRatePlanAmt(BigDecimal clpRatePlanAmt) {
      this.clpRatePlanAmt = clpRatePlanAmt;
      this.clpRatePlanAmtDirty = true;
   }

   public Integer getClpContractTerm() {
      return clpContractTerm;
   }
   
   public void setClpContractTerm(Integer clpContractTerm) {
      this.clpContractTerm = clpContractTerm;
      this.clpContractTermDirty = true;
   }

   public BigDecimal getClpCreditLimit() {
      return clpCreditLimit;
   }
   public void setClpCreditLimit(BigDecimal clpCreditLimit) {
      this.clpCreditLimit = clpCreditLimit;
      this.clpCreditLimitDirty = true;
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
   public String getAssessmentMessageCode() {
      return assessmentMessageCode;
   }
   public void setAssessmentMessageCode(String assessmentMessageCode) {
      this.assessmentMessageCode = assessmentMessageCode;
      this.assessmentMessageCodeDirty = true;
   }

   @Exclude
   public String getAssessmentMessage() {
      return assessmentMessage;
   }
   @Exclude
   public void setAssessmentMessage(String assessmentMessage) {
      this.assessmentMessage = assessmentMessage;
      this.assessmentMessageDirty = true;
   }

   @Exclude
   public String getAssessmentMessage_fr() {
      return assessmentMessage_fr;
   }
   @Exclude
   public void setAssessmentMessage_fr(String assessmentMessage_fr) {
      this.assessmentMessage_fr = assessmentMessage_fr;
   }

   public List<TelusCreditDecisionWarning> getWarningHistoryList() {
      return warningHistoryList;
   }
   public void setWarningHistoryList(List<TelusCreditDecisionWarning> warningHistoryList) {
      this.warningHistoryList = warningHistoryList;
      this.warningHistoryListDirty = true;
   }
   
	@JsonIgnore 
	public Long getCreditProfileLegacyId() { 
		return creditProfileLegacyId;
	
	} 
	public void setCreditProfileLegacyId(Long creditProfileLegacyId) { 
		 this.creditProfileLegacyId = creditProfileLegacyId;
	 this.creditProfileLegacyIdDirty = true; 
	 }
	 
   public Long getCreditAssessmentId() {
      return creditAssessmentId;
   }
   public void setCreditAssessmentId(Long creditAssessmentId) {
      this.creditAssessmentId = creditAssessmentId;
      this.creditAssessmentIdDirty = true;
   }

	
	 @JsonIgnore
	  
	 @Exclude public PatchField<Long> getCreditProfileLegacyIdPatch() { 
		 return PatchField.patchOrNull(creditProfileLegacyIdDirty, creditProfileLegacyId); 
}
	 

   @JsonIgnore
   @Exclude
   public PatchField<String> getPrimaryCreditScoreCdPatch() {
      return PatchField.patchOrNull(primaryCreditScoreCdDirty, primaryCreditScoreCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getPrimaryCreditScoreTypeCdPatch() {
      return PatchField.patchOrNull(primaryCreditScoreTypeCdDirty, primaryCreditScoreTypeCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getBureauDecisionCodePatch() {
      return PatchField.patchOrNull(bureauDecisionCodeDirty, bureauDecisionCode);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getBureauDecisionMessagePatch() {
      return PatchField.patchOrNull(bureauDecisionMessageDirty, bureauDecisionMessage);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditProgramNamePatch() {
      return PatchField.patchOrNull(creditProgramNameDirty, creditProgramName);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditClassCdPatch() {
      return PatchField.patchOrNull(creditClassCdDirty, creditClassCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditClassDatePatch() {
      return PatchField.patchOrNull(creditClassDateDirty, creditClassDate);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditDecisionCdPatch() {
      return PatchField.patchOrNull(creditDecisionCdDirty, creditDecisionCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getCreditDecisionDatePatch() {
      return PatchField.patchOrNull(creditDecisionDateDirty, creditDecisionDate);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getRiskLevelNumberPatch() {
      return PatchField.patchOrNull(riskLevelNumberDirty, riskLevelNumber);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getRiskLevelDecisionCdPatch() {
      return PatchField.patchOrNull(riskLevelDecisionCdDirty, riskLevelDecisionCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getRiskLevelDtPatch() {
      return PatchField.patchOrNull(riskLevelDtDirty, riskLevelDt);
   }

   @JsonIgnore
   @Exclude
   public PatchField<BigDecimal> getClpRatePlanAmtPatch() {
      return PatchField.patchOrNull(clpRatePlanAmtDirty, clpRatePlanAmt);
   }

   @JsonIgnore
   @Exclude
   public PatchField<Integer> getClpContractTermPatch() {
      return PatchField.patchOrNull(clpContractTermDirty, clpContractTerm);
   }

   @JsonIgnore
   @Exclude
   public PatchField<BigDecimal> getClpCreditLimitPatch() {
      return PatchField.patchOrNull(clpCreditLimitDirty, clpCreditLimit);
   }

   @JsonIgnore
   @Exclude
   public PatchField<BigDecimal> getAverageSecurityDepositAmtPatch() {
      return PatchField.patchOrNull(averageSecurityDepositAmtDirty, averageSecurityDepositAmt);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getAssessmentMessageCodePatch() {
      return PatchField.patchOrNull(assessmentMessageCodeDirty, assessmentMessageCode);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getAssessmentMessagePatch() {
      return PatchField.patchOrNull(assessmentMessageDirty, assessmentMessage);
   }

   @JsonIgnore
   @Exclude
   public PatchField<Long> getCreditAssessmentIdPatch() {
      return PatchField.patchOrNull(creditAssessmentIdDirty, creditAssessmentId);
   }

   @JsonIgnore
   @Exclude
   public PatchField<List<TelusCreditDecisionWarning>> getWarningHistoryListPatch() {
      return PatchField.patchOrNull(warningHistoryListDirty, warningHistoryList);
   }

   @Override
	public String toString() {
      return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
