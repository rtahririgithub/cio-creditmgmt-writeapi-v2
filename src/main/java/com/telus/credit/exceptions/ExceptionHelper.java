package com.telus.credit.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.telus.credit.model.BaseResponse;
import com.telus.credit.model.ErrorResponse;

@Component
public class ExceptionHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHelper.class);
   public static ResponseEntity<BaseResponse> createErrorResponse(CreditException ce) {
      ErrorResponse er = new ErrorResponse();
      er.setCode(ce.getCode());
      er.setReason(ce.getReason());
      er.setMessage(ce.getMessage());
      HttpStatus httpStatus = ce.getHttpStatus();
      return new ResponseEntity<>(er, httpStatus);
   }
 
   
   public static ResponseEntity<BaseResponse> createErrorResponse(Exception e) {
      ErrorResponse er = new ErrorResponse();      
      HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;  // httpstatus 500
      er.setCode("1000");
      er.setReason("Unexpected error occurred");
      er.setMessage(e.getMessage());
      if ( e instanceof NullPointerException ) {
    	  er.setMessage("NullPointerException");
      }
      return new ResponseEntity<>(er, httpStatus);
   }
   
   public static ResponseEntity<BaseResponse> createErrorResponse(Throwable e) {
	      ErrorResponse er = new ErrorResponse();      
	      HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;  // httpstatus 500
	      er.setCode("1000");
	      er.setReason("Unexpected error occurred");
	      er.setMessage(e.getMessage());
	      return new ResponseEntity<>(er, httpStatus);
	   }  
   
   
   public static ResponseEntity<BaseResponse> createGenericErrorResponse() {
      ErrorResponse er = new ErrorResponse();      
      HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;  // httpstatus 500
      er.setCode("1000");
      er.setReason("Unexpected generic error occurred");
      return new ResponseEntity<>(er, httpStatus);
   }
   
   public static CreditException createGenericCreditException(String message) {
      CreditException ce = new CreditException(HttpStatus.INTERNAL_SERVER_ERROR, 
            ExceptionConstants.ERR_CODE_1000, ExceptionConstants.ERR_CODE_1000_MSG, message);      
      return ce;
   }
   
   
   public static   String removeBrkLine(String str) {
		if(str!=null && !str.isEmpty()){
			try{
				str = str.replaceAll("\\r\\n|\\r|\\n", " ");
			}catch (Throwable e){}
		}
		return str;
	}

   public static   String leadingTrailingEscapeChar( String str) {
		if(str!=null && !str.isEmpty()){
			try{
			   str = str.startsWith("\"") ? str.substring(1) : str;
			   str = str.endsWith("\"") ? str.substring(0,str.length()-1) : str;
			}catch (Throwable e){}
		}
		return str;
   }
   
   
	public static String getStackTrace(Throwable t) {
		String stckTraceStr ="getStackTrace:";
		try{
			if(t!= null){
				String exceptionclassName = (t.getClass()!=null)?t.getClass().getName():"";
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw, true);
				t.printStackTrace(pw);
				pw.flush();
				sw.flush();
				stckTraceStr ="[exceptionclass:" + exceptionclassName+ " , StackTrace : "  + 	sw.toString()  +"EndOfStackTrace]";
				stckTraceStr=removeBrkLine(stckTraceStr);
				stckTraceStr=leadingTrailingEscapeChar(stckTraceStr);		
			}
		}catch (Throwable e){}
		return stckTraceStr;
	}  
	
	
}
