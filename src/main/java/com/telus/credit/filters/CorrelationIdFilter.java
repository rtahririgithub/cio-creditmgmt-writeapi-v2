package com.telus.credit.filters;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.telus.credit.common.CreditMgmtCommonConstants;
import com.telus.credit.exceptions.ExceptionConstants;

@Order(10)
@Component
public class CorrelationIdFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(CorrelationIdFilter.class);
	
	@Value("#{${authorized.client.ids}}")
	private Set<String> authClients;
	

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		
			// Setup MDC data:
			// Get correlationId
			String correlationId = httpRequest.getHeader(CreditMgmtCommonConstants.HEADER_CORR_ID);
			if (!validateToken(httpRequest)) {
				httpResponse.setContentType("application/json");
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				httpResponse.getWriter().write(ExceptionConstants.ERR_CODE_401_MSG);
				httpResponse.flushBuffer();
				return;
			}
			if (StringUtils.isBlank(correlationId)) {
				correlationId = UUID.randomUUID().toString().toLowerCase();
			} else {
				correlationId = HtmlUtils.htmlEscape(correlationId);
			}

			// Kong auto-injects Kong-Request-ID to trace calls too
			String kongRequestId = httpRequest.getHeader("Kong-Request-ID");
			if (!StringUtils.isBlank(kongRequestId)) {
				MDC.put("requestId", kongRequestId);
			}

			MDC.put("correlationId", correlationId);
			MDC.put("containerId", InetAddress.getLocalHost().getHostName());

			logger.trace("CorrelationIdFilter.doFilter() called for:{} ", httpRequest.getRequestURI());

			httpRequest.setAttribute(CreditMgmtCommonConstants.HEADER_CORR_ID, correlationId);
			httpResponse.addHeader(CreditMgmtCommonConstants.HEADER_CORR_ID, correlationId);
			


	        chain.doFilter(httpRequest, httpResponse);
		} catch (Throwable e) {
				logger.warn("doFilter failed.",e);
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request.");
		}finally {
			// Tear down MDC data:
			MDC.clear();
		}
	}

	@Override
	public void destroy() {
		// Destroy is not required
	}

	@Override
	public void init(FilterConfig arg0) {
		// Initialization is not required
	}

	/**
	 * Doing a minimalistic check against the token as KONG gateway takes care of
	 * Token Validity. Adding a check against the aud and expiry claims of the Token
	 * 
	 * @param httpRequest
	 * @return
	 */
	private boolean validateToken(HttpServletRequest httpRequest) {
		// Skip validation for actuator,greeting endpoints
		if (
				StringUtils.containsIgnoreCase(httpRequest.getRequestURI(), "/actuator")
				||
				(StringUtils.containsIgnoreCase(httpRequest.getRequestURI(), "/greeting"))
				||
				(StringUtils.containsIgnoreCase(httpRequest.getRequestURI(), "/version"))				
			) 
		{
			return true;
		}
	
		
		String auth = httpRequest.getHeader(CreditMgmtCommonConstants.HEADER_AUTHORIZATION);
		boolean valid = false;
		if (StringUtils.isNotBlank(auth) && StringUtils.startsWith(auth, CreditMgmtCommonConstants.BEARER)) {
			String[] token = auth.split("\\.");
			if (StringUtils.isNotEmpty(token[1])) {
				String content = new String(Base64.getDecoder().decode(token[1]));
				if (StringUtils.containsIgnoreCase(content, "kong")) {
					long curTm = System.currentTimeMillis();
					// As per KONG document it says to add an extra 10 seconds check for expiry
					long validityNum = Long.parseLong(
							StringUtils.substringAfterLast(content, "\"exp\":").replace("}", StringUtils.EMPTY)) * 1000
							+ 10000;
					logger.debug("tokenValidity:{}, currTime:{}", validityNum, curTm);
					valid = validityNum >= curTm;
				}
			}
		}
		logger.info("valid token:{}", valid);
		return valid;
	}
}