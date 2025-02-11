package com.telus.credit.firestore.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.telus.credit.common.DtoRefectionToStringStyle;

@JsonInclude(Include.NON_NULL)
public class AssesmentDocumentCompact {
	
	private String customerId;
	private String assessmentMessageCd;
	
	
	public String getCustomerId() {
		return customerId;
	}


	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}


	public String getAssessmentMessageCd() {
		return assessmentMessageCd;
	}


	public void setAssessmentMessageCd(String assessmentMessageCd) {
		this.assessmentMessageCd = assessmentMessageCd;
	}



	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
