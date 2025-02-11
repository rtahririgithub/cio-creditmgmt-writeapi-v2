package com.telus.credit.exceptions;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.telus.credit.model.BaseResponse;

@ControllerAdvice
public class ErrorController {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse> handleException(HttpMessageNotReadableException exception, HttpServletRequest request) {
       return ExceptionHelper.createErrorResponse(
    		   new CreditException(
    				   HttpStatus.BAD_REQUEST,
    				   ExceptionConstants.ERR_CODE_1000, 
    				   ExceptionConstants.ERR_CODE_1000_MSG, 
    				    exception.getMessage())
    		   );
    }
}