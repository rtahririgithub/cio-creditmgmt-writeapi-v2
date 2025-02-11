package com.telus.credit.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.common.WarningStatusCode;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.validation.ValidCreditWarningCategory;
import com.telus.credit.validation.ValidEnum;
import com.telus.credit.validation.ValidTimePeriod;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

@JsonInclude(Include.NON_NULL)
public class TelusCreditDecisionWarning {
   private String id;
   private Long warningHistoryLegacyId;
   private Long warningHistoryLegacyIdNew;

   //@NotBlank(groups = Create.class, message = "1116")
   @ValidCreditWarningCategory(groups = {Create.class, Patch.class})
   private String warningCategoryCd;

   private String warningCd;

   private String warningTypeCd;

   private String warningItemTypeCd;

   @NotBlank(groups = {Create.class}, message = "1117")
   @ValidEnum(enumClass = WarningStatusCode.class, groups = {Create.class, Patch.class}, message = "1117")
   private String warningStatusCd;

   //@NotBlank(groups = {Create.class}, message = "1118")
   @JsonProperty("warningDetectionTs")
   private String warningDetectionTs;

   @Pattern(regexp = "(0)|(1)|(true)|(false)", groups = {Create.class, Patch.class})
   private String resolvedPermanentlyInd;
   
   @JsonProperty("approvalCreditAssessmentId")
   private Long approvalCreditAssessmentId;
   
   @JsonProperty("approvalTs")
   private String approvalTs;
   
   private Long approvalExternalId;
   
   //private String memoTypeCd;
   
   @ValidTimePeriod(groups = {Create.class, Patch.class})
   private TimePeriod validFor;

   @JsonIgnore
   private boolean warningHistoryLegacyIdDirty = false;
   @JsonIgnore
   private boolean warningCategoryCdDirty = false;
   @JsonIgnore
   private boolean warningCdDirty = false;
   @JsonIgnore
   private boolean warningTypeCdDirty = false;
   @JsonIgnore
   private boolean warningItemTypeCdDirty = false;
   @JsonIgnore
   private boolean warningDetectionDateDirty = false;
   @JsonIgnore
   private boolean warningStatusCdDirty = false;
   @JsonIgnore
   private boolean resolvedPermanentlyIndDirty = false;
   @JsonIgnore
   private boolean creditAssessmentIdDirty = false;
   @JsonIgnore
   private boolean approvalDateDirty = false;
   @JsonIgnore
   private boolean approvalExternalIdDirty = false;
   @JsonIgnore
   private boolean memoTypeCdDirty = false;
   @JsonIgnore
   private boolean validForDirty = false; 
   
   public String getId() {
      return id;
   }
   public void setId(String warningHistoryId) {
      this.id = warningHistoryId;
   }
   public String getWarningCategoryCd() {
      return warningCategoryCd;
   }
   public void setWarningCategoryCd(String warningCategoryCd) {
      this.warningCategoryCd = warningCategoryCd;
      this.warningCategoryCdDirty = true;
   }

   public String getWarningCd() {
      return warningCd;
   }
   public void setWarningCd(String warningCd) {
      this.warningCd = warningCd;
      this.warningCdDirty = true;
   }

   public String getWarningTypeCd() {
      return warningTypeCd;
   }
   public void setWarningTypeCd(String warningTypeCd) {
      this.warningTypeCd = warningTypeCd;
      this.warningTypeCdDirty = true;
   }

   public String getWarningItemTypeCd() {
      return warningItemTypeCd;
   }
   public void setWarningItemTypeCd(String warningItemTypeCd) {
      this.warningItemTypeCd = warningItemTypeCd;
      this.warningItemTypeCdDirty = true;
   }

   public String getWarningDetectionTs() {
      return warningDetectionTs;
   }
   public void setWarningDetectionTs(String warningDetectionDate) {
      this.warningDetectionTs = warningDetectionDate;
      this.warningDetectionDateDirty = true;
   }
   public String getWarningStatusCd() {
      return warningStatusCd;
   }
   public void setWarningStatusCd(String warningStatusCd) {
      this.warningStatusCd = warningStatusCd;
      this.warningStatusCdDirty = true;
   }

   public String getResolvedPermanentlyInd() {
      return resolvedPermanentlyInd;
   }
   public void setResolvedPermanentlyInd(String resolvedPermanentlyInd) {
      this.resolvedPermanentlyInd = resolvedPermanentlyInd;
      this.resolvedPermanentlyIndDirty = true;
   }
   public TimePeriod getValidFor() {
      return validFor;
   }
   public void setValidFor(TimePeriod validFor) {
      this.validFor = validFor;
      this.validForDirty = true;
   }

	
	 public Long getWarningHistoryLegacyId() { 
		 return warningHistoryLegacyId; 
		}
	 public void setWarningHistoryLegacyId(Long warningHistoryLegacyId) {
		 this.warningHistoryLegacyId = warningHistoryLegacyId;
		 this.warningHistoryLegacyIdDirty = true; 
		}
	 public Long getWarningHistoryLegacyIdNew() { 
		return warningHistoryLegacyIdNew; 
	 } 	 
	 public void setWarningHistoryLegacyIdNew(Long warningHistoryLegacyIdNew) { 
		 this.warningHistoryLegacyIdNew = warningHistoryLegacyIdNew; 
	 }
	 

   public Long getApprovalCreditAssessmentId() {
      return approvalCreditAssessmentId;
   }
   public void setApprovalCreditAssessmentId(Long creditAssessmentId) {
      this.approvalCreditAssessmentId = creditAssessmentId;
      this.creditAssessmentIdDirty = true;
   }
   public String getApprovalTs() {
      return approvalTs;
   }
   public void setApprovalTs(String approvalDate) {
      this.approvalTs = approvalDate;
      this.approvalDateDirty = true;
   }
   public Long getApprovalExternalId() {
      return approvalExternalId;
   }
   public void setApprovalExternalId(Long approvalExternalId) {
      this.approvalExternalId = approvalExternalId;
      this.approvalExternalIdDirty = true;
   }
	/*
	 public String getMemoTypeCd() { return memoTypeCd; } public void
	 setMemoTypeCd(String memoTypeCd) { this.memoTypeCd = memoTypeCd;
	 this.memoTypeCdDirty = true; }
	 */
	

	
	 @JsonIgnore
	 @Exclude public PatchField<Long> getWarningHistoryLegacyIdPatch() { return
	 PatchField.patchOrNull(warningHistoryLegacyIdDirty, warningHistoryLegacyId);
	 }
	 

   @JsonIgnore
   @Exclude
   public PatchField<@NotBlank(groups = {Patch.class}, message = "1116") String> getWarningCategoryCdPatch() {
      return PatchField.patchOrNull(warningCategoryCdDirty, warningCategoryCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getWarningCdPatch() {
      return PatchField.patchOrNull(warningCdDirty, warningCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getWarningTypeCdPatch() {
      return PatchField.patchOrNull(warningTypeCdDirty, warningTypeCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getWarningItemTypeCdPatch() {
      return PatchField.patchOrNull(warningItemTypeCdDirty, warningItemTypeCd);
   }

   @JsonIgnore
   @Exclude
   /*
   //removed @NotBlank warningDetectionTs validation.
   public PatchField<@NotBlank(groups = {Patch.class}, message = "1118") String> getWarningDetectionDatePatch1() {
      return PatchField.patchOrNull(warningDetectionDateDirty, warningDetectionTs);
   }
   */
   public PatchField<String> getWarningDetectionDatePatch() {	   
	    return PatchField.patchOrNull(warningDetectionDateDirty, warningDetectionTs);
	}
   
   
   @JsonIgnore
   @Exclude
   public PatchField<@NotBlank(groups = {Patch.class}, message = "1117") String> getWarningStatusCdPatch() {
      return PatchField.patchOrNull(warningStatusCdDirty, warningStatusCd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getResolvedPermanentlyIndPatch() {
      return PatchField.patchOrNull(resolvedPermanentlyIndDirty, resolvedPermanentlyInd);
   }

   @JsonIgnore
   @Exclude
   public PatchField<Long> getCreditAssessmentIdPatch() {
      return PatchField.patchOrNull(creditAssessmentIdDirty, approvalCreditAssessmentId);
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getApprovalDatePatch() {
      return PatchField.patchOrNull(approvalDateDirty, approvalTs);
   }

   @JsonIgnore
   @Exclude
   public PatchField<Long> getApprovalExternalIdPatch() {
      return PatchField.patchOrNull(approvalExternalIdDirty, approvalExternalId);
   }

	/*
	 * @JsonIgnore
	 * 
	 * @Exclude public PatchField<String> getMemoTypeCdPatch() { return
	 * PatchField.patchOrNull(memoTypeCdDirty, memoTypeCd); }
	 */
   @JsonIgnore
   @Exclude
   public PatchField<TimePeriod> getValidForPatch() {
      return PatchField.patchOrNull(validForDirty, validFor);
   }

   @Override
	public String toString() {
      return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
