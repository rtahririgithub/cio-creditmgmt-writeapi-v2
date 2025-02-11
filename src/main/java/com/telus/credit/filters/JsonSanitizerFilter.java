package com.telus.credit.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;

@Order(11)
@Component
public class JsonSanitizerFilter implements Filter {

    public static final String[] SUPPORTED_METHODS = {"PATCH", "POST", "PUT"};
    private static final Log LOGGER = LogFactory.getLog(JsonSanitizerFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            if (ArrayUtils.contains(SUPPORTED_METHODS, httpServletRequest.getMethod()) &&
                    httpServletRequest.getContentLengthLong() > 0L &&
                    StringUtils.isNotBlank(httpServletRequest.getContentType()) &&
                    httpServletRequest.getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE)) {
                request = new JsonSanitizerRequestWrapper(httpServletRequest);
            }
	        try {
	        	chain.doFilter(request, response);
			} catch (Throwable e) {
				LOGGER.warn("doFilter failed.",e);
			}	           
        } catch (Throwable e) {
            LOGGER.error( ExceptionConstants.STACKDRIVER_METRIC +":" +  ExceptionHelper.getStackTrace(e));
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request.");
        }
    }
}
