package com.telus.credit.exceptions;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.http.HttpStatus;

import com.telus.credit.common.LangHelper;

public class CreditException extends RuntimeException {
	private static final long serialVersionUID = -744690390571315712L;

	private HttpStatus httpStatus;
	private String code;     // Mandatory
	private String reason;   // Mandatory. Text that explains the reason for error. This can be shown to a client user.
	private String message;  // Additional info
	private String details;
	public CreditException(HttpStatus httpStatus, String code, String reason, String message) {
		super(code + " " + LangHelper.getMessage(code));
		this.httpStatus = httpStatus;
		this.code = code;
		this.reason = reason;
		this.message = message;
	}
	public CreditException(HttpStatus httpStatus, String code, String reason, String message,String details) {
		super(code + " " + LangHelper.getMessage(code));
		this.httpStatus = httpStatus;
		this.code = code;
		this.reason = reason;
		this.message = message;
		this.details = details;
	}
   public HttpStatus getHttpStatus() {
      return httpStatus;
   }

   public void setHttpStatus(HttpStatus httpStatus) {
      this.httpStatus = httpStatus;
   }

   public String getCode() {
      return code;
   }

   public void setCode(String code) {
      this.code = code;
   }

   public String getReason() {
      return reason;
   }

   public void setReason(String reason) {
      this.reason = reason;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }
   
   public String getDetails() {
	      return details;
	   }

   public void setDetails(String details) {
      this.details = details;
   }   
   @Override
   public String toString() {
       return new ToStringBuilder(this)
               .append("httpStatus", httpStatus)
               .append("code", code)
               .append("reason", reason)
               .append("message", message)
               .append("details", details)
               .toString();
   }
}