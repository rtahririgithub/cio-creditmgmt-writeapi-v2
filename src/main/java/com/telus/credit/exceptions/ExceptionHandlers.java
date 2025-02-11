package com.telus.credit.exceptions;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.support.RequestContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.common.ErrorCode;
import com.telus.credit.model.BaseResponse;
import com.telus.credit.model.ErrorResponse;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.service.impl.AuditService;

@ControllerAdvice
public class ExceptionHandlers  extends ResponseEntityExceptionHandler implements RequestBodyAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlers.class);
    private static final ThreadLocal<ExceptionHandlers> requestInfoThreadLocal = new ThreadLocal<>();

    private String method;
    private String body;
    private String queryString;
    private String ip;
    private String user;
    private String referrer;
    private String url;
    private Object reqBody;
    
    public ExceptionHandlers(AuditService auditService, ObjectMapper objectMapper) {
    }

    public ExceptionHandlers() {
		// TODO Auto-generated constructor stub
	}

	@Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
      ErrorResponse er = new ErrorResponse();      
      HttpStatus httpStatus = HttpStatus.BAD_REQUEST;  
      er.setCode("1000");
      er.setReason("bad request");
      er.setMessage(ex.getMessage());  

      Exception exception = new Exception(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
      ErrorCode code = ErrorCode.from(exception.getMessage());
      if(code!=null) {
       er.setCode(exception.getMessage());
       er.setReason(code.getMessage());  
       er.setMessage(code.getMessage());  
      }

     return new ResponseEntity<>(er, httpStatus);    
    }     


   

    @ExceptionHandler(value = {DataAccessException.class, CannotCreateTransactionException.class})
    public ResponseEntity<BaseResponse> handleDataAccessException(Exception de) {
    	String msg=de.getMessage();
    	if (de instanceof IncorrectResultSizeDataAccessException) {
	    	msg= "[" ;
	    	msg = msg  + "ActualSize=" + ((IncorrectResultSizeDataAccessException)de).getActualSize();
	    	msg = msg  + ",ExpectedSize=" +((IncorrectResultSizeDataAccessException)de).getExpectedSize();
	    	msg = msg  + ",Message=" + ((IncorrectResultSizeDataAccessException)de).getMessage();
	    	msg = msg  + "]";
    	}
        LOGGER.error("{}: {} Data Access Exception. msg: {} .StackTrace: {}",  ExceptionConstants.STACKDRIVER_METRIC,ExceptionConstants.POSTGRES100,msg, ExceptionHelper.getStackTrace(de));
        
        ResponseEntity<BaseResponse> errorResponse = ExceptionHelper.createErrorResponse(new CreditException(HttpStatus.INTERNAL_SERVER_ERROR,
                ExceptionConstants.ERR_CODE_8000, ExceptionConstants.ERR_CODE_8000_MSG, de.getMessage()));
        if (errorResponse.getBody() != null) {
            persistAudit(errorResponse);
        }

        return errorResponse;
    }

    @ExceptionHandler(value = CreditException.class)
    public ResponseEntity<BaseResponse> handleCreditException(CreditException ce) {        
    	int exceptionHttpStatusVal = (ce.getHttpStatus()!=null)?ce.getHttpStatus().value():0;
       	if(	HttpStatus.BAD_REQUEST.value() !=exceptionHttpStatusVal && HttpStatus.NOT_FOUND.value() !=exceptionHttpStatusVal) {
    		LOGGER.error("{}: CreditException: {} . {}", ExceptionConstants.STACKDRIVER_METRIC,ce.toString(), ExceptionHelper.getStackTrace(ce));
    	}else {
    		LOGGER.warn("CreditException: {}. {} ", ce.toString() , ExceptionHelper.getStackTrace(ce));
    	}
    	
        ResponseEntity<BaseResponse> errorResponse = ExceptionHelper.createErrorResponse(ce);
        if (ce.getHttpStatus() != HttpStatus.BAD_REQUEST && errorResponse.getBody() != null) {
            persistAudit(errorResponse);
        }
        return errorResponse;
    }

    @ExceptionHandler(value = DateTimeParseException.class)
    public ResponseEntity<BaseResponse> handleDateTimeParseException(DateTimeParseException de) {
        LOGGER.warn("{} DateTimeParse exception. {}", ExceptionConstants.DATAVALIDATION100, ExceptionHelper.getStackTrace(de));
        return ExceptionHelper.createErrorResponse(new CreditException(HttpStatus.BAD_REQUEST,
                ErrorCode.C_1119.code(), ErrorCode.C_1119.getMessage(), de.getMessage()));
    }

    @ExceptionHandler(value = NumberFormatException.class)
    public ResponseEntity<BaseResponse> handleNumberFormatException(NumberFormatException ne) {
        LOGGER.warn("{} NumberFormatException exception. {}", ExceptionConstants.DATAVALIDATION101, ExceptionHelper.getStackTrace(ne));
        return ExceptionHelper.createErrorResponse(new CreditException(HttpStatus.BAD_REQUEST,
                ExceptionConstants.ERR_CODE_1000, ExceptionConstants.ERR_CODE_1000_MSG, "Invalid number " + ne.getMessage().replaceFirst("For", "for")));
    }
    
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<BaseResponse> handleConstraintViolationException(ConstraintViolationException cve) {
    	LOGGER.warn("{} ConstraintViolationException. {}", ExceptionConstants.DATAVALIDATION101, ExceptionHelper.getStackTrace(cve));
    	String code[] = StringUtils.split(cve.getMessage(), ":");
    	if(ObjectUtils.isNotEmpty(code) && code.length > 1 && ObjectUtils.isNotEmpty(ErrorCode.from(StringUtils.trim(code[1])))) {
    		return ExceptionHelper.createErrorResponse(new CreditException(HttpStatus.BAD_REQUEST,
    				StringUtils.trim(code[1]), ErrorCode.from(StringUtils.trim(code[1])).getMessage(),code[0]));
    	}
    	return ExceptionHelper.createErrorResponse(new CreditException(HttpStatus.BAD_REQUEST,
    			ExceptionConstants.ERR_CODE_1000, ExceptionConstants.ERR_CODE_1000_MSG, cve.getMessage()));
    }

    @ExceptionHandler(value = Throwable.class)
    public ResponseEntity<BaseResponse> handleThrowableException(Throwable exception) {
        LOGGER.error("{}Exception. {}", ExceptionConstants.STACKDRIVER_METRIC,ExceptionHelper.getStackTrace(exception));
        ResponseEntity<BaseResponse> errorResponse = ExceptionHelper.createErrorResponse(exception);
        return errorResponse;
    } 
    

    
    /*
    @ExceptionHandler(value = Exception.class)   
    public ResponseEntity<BaseResponse> handleException(Exception exception, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {    	
        LOGGER.error("Exception. {}", ExceptionHelper.getStackTrace(exception));
        ResponseEntity<BaseResponse> errorResponse = ExceptionHelper.createErrorResponse(exception);
        if (errorResponse.getBody() != null) {
            persistAudit(errorResponse);
        }
        return errorResponse;
    }
    */
/*    
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<BaseResponse> handleException(Exception exception,
    		javax.servlet.ServletRequest aServletRequest, 
    		javax.servlet.http.HttpServletRequest aHttpServletRequest ) {  
    	
        LOGGER.error("Exception. {}", ExceptionHelper.getStackTrace(exception));
        ResponseEntity<BaseResponse> errorResponse = ExceptionHelper.createErrorResponse(exception);
        if (errorResponse.getBody() != null) {
            persistAudit(errorResponse);
        }
        return errorResponse;
    } 
    
 */
/*
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<BaseResponse> handleException(Exception exception) {  	
    	// Object requestBody = requestContext.getRequestBody();
        LOGGER.error("Exception. {}", ExceptionHelper.getStackTrace(exception));
        ResponseEntity<BaseResponse> errorResponse = ExceptionHelper.createErrorResponse(exception);
        if (errorResponse.getBody() != null) {
            persistAudit(errorResponse);
        }
        return errorResponse;
    }  
 */   
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleNoSuchElementException(final Exception exception, final WebRequest request) {
        String custId="NA";
         try {
        	 if ( reqBody instanceof com.telus.credit.pubsub.model.TelusCreditProfilePubSubEvent) {
        		 custId = ((com.telus.credit.pubsub.model.TelusCreditProfilePubSubEvent)reqBody).getEvent().getCustomerRelatedParty().getId();
        	 }else {
        		 if ( reqBody instanceof TelusCreditProfile) {
        			 custId = ((TelusCreditProfile)reqBody).getCustomerRelatedParty().getId(); 
        		 }
        	 }
			
		} catch (Exception e) {}
 
        LOGGER.error("{} CustId={} ,  StackTrace: {}", ExceptionConstants.STACKDRIVER_METRIC, custId ,  ExceptionHelper.getStackTrace(exception));
        ResponseEntity<BaseResponse> errorResponse = ExceptionHelper.createErrorResponse(exception);
        if (errorResponse.getBody() != null) {
            persistAudit(errorResponse);
        }
        return errorResponse;
    } 
    
     @ExceptionHandler(value = com.fasterxml.jackson.core.JsonParseException.class)
    public ResponseEntity<BaseResponse> handleJsonParseException(com.fasterxml.jackson.core.JsonParseException ce) {        
         ErrorResponse er = new ErrorResponse();      
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;  
        er.setCode("1000");
        er.setReason("bad request");
        er.setMessage("bad request");
        return new ResponseEntity<>(er, httpStatus);

    }    
    private void persistAudit(ResponseEntity<BaseResponse> errorResponse) {
        // Do nothing right now
//        CreditProfileAuditDocument auditDocument = AuditService.auditContext();
//        if (auditDocument.getEventType() == null) {
//            return;
//        }
//
//        try {
//            auditDocument.setError(objectMapper.writeValueAsString(errorResponse.getBody()));
//            auditService.addAuditLog(auditDocument);
//        } catch (JsonProcessingException e) {
//            throw new IllegalStateException(e);
//        }
    }

    public static ExceptionHandlers get() {
        ExceptionHandlers requestInfo = requestInfoThreadLocal.get();
        if (requestInfo == null) {
            requestInfo = new ExceptionHandlers();
            requestInfoThreadLocal.set(requestInfo);
        }
        return requestInfo;
    }

    public Map<String,String> asMap() {
        Map<String,String> map = new HashMap<>();
        map.put("method", this.method);
        map.put("url", this.url);
        map.put("queryParams", this.queryString);
        map.put("body", this.body);
        map.put("ip", this.ip);
        map.put("referrer", this.referrer);
        map.put("user", this.user);
        return map;
    }

    private void setInfoFromRequest(HttpServletRequest request) {
        this.method = request.getMethod();
        this.queryString = request.getQueryString();
        this.ip = request.getRemoteAddr();
        this.referrer = request.getRemoteHost();
        this.url = request.getRequestURI();
        if (request.getUserPrincipal() != null) {
            this.user = request.getUserPrincipal().getName();
        }
    }

    public void setBody(String body) {
        this.body = body;
    }

    private static void setInfoFrom(HttpServletRequest request) {
        ExceptionHandlers requestInfo = requestInfoThreadLocal.get();
        if (requestInfo == null) {
            requestInfo = new ExceptionHandlers();
        }
        requestInfo.setInfoFromRequest(request);
        requestInfoThreadLocal.set(requestInfo);
    }

    private static void clear() {
        requestInfoThreadLocal.remove();
    }

    private static void setBodyInThreadLocal(String body) {
        ExceptionHandlers requestInfo = get();
        requestInfo.setBody(body);
        setRequestInfo(requestInfo);
    }

    private static void setRequestInfo(ExceptionHandlers requestInfo) {
        requestInfoThreadLocal.set(requestInfo);
    }

    // Implementation of HandlerInterceptorAdapter to capture the request info (except body) and be able to add it to the report in case of an error

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        ExceptionHandlers.setInfoFrom(request);
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        ExceptionHandlers.clear();
    }

    // Implementation of RequestBodyAdvice to capture the request body and be able to add it to the report in case of an error

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        ExceptionHandlers.setBodyInThreadLocal(body.toString());
        this.reqBody = body;
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
    
}
