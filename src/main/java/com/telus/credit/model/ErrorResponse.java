package com.telus.credit.model;

import io.swagger.annotations.ApiModelProperty;

public class ErrorResponse extends BaseResponse{
	private static final long serialVersionUID = -2879278283158221831L;

	private String code;      // TMF says this is mandatory
	private String reason;    // TMF says this is mandatory
	private String message;   // TMF says this is optional

	@ApiModelProperty(value = "Application error code", required = true)
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	@ApiModelProperty(value = "Text that explains the reason for error. This can be shown to a client user.", required = true)
   public String getReason() {
      return reason;
   }
   public void setReason(String reason) {
      this.reason = reason;
   }
   
	@ApiModelProperty(value = "Text that provide more details and corrective actions related to the error. This can be shown to a client user.", required = false)
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
