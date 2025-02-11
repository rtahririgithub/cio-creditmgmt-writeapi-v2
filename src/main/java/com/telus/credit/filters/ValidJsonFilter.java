/*
package com.telus.credit.filters;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.common.MultiReadHttpServletRequest;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@Order(1)
@Component
public class ValidJsonFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(ValidJsonFilter.class);
 
	private static final Log LOGGER = LogFactory.getLog(ValidJsonFilter.class);

    private ObjectMapper objectMapper;
    
    public ValidJsonFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }    
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
    		throws IOException, ServletException {
    	logger.info("ValidJsonFilter");
    	try {    	
    	HttpServletRequest httpRequest = (HttpServletRequest) request;

        HttpServletRequestWrapper wrappedRequest = new MultiReadHttpServletRequest(httpRequest);
        Map<String, Object> jsonBody = objectMapper.readValue(wrappedRequest.getInputStream(), new TypeReference<Map<String, Object>>() {});
    	chain.doFilter(request, response);
		} catch (Throwable e) {
			LOGGER.warn("doFilter failed.",e);
			//throw e;
			//((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());
		}     	
    }
}

*/
