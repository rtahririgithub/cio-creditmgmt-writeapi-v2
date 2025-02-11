package com.telus.credit.filters;

import static org.springframework.integration.json.ObjectToJsonTransformer.JSON_CONTENT_TYPE;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.common.MultiReadHttpServletRequest;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.service.impl.AuditService;

/**
 * Add request json into audit context for audit purpose.
 * All secured node will be encrypted
 */
@Order(11)
@Component
public class AuditLogFilter implements Filter {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogFilter.class);
    private static final String[] SECURED_NODES = new String[] {
            "creditProfile.creditRiskRating",
            "creditProfile.creditScore",
            "creditProfile.telusCharacteristic.creditClassCd",
            "creditProfile.telusCharacteristic.creditDecisionCd",
            "creditProfile.telusCharacteristic.riskLevelDecisionCd",
            "creditProfile.telusCharacteristic.warningHistoryList.warningCategoryCd",
            "creditProfile.telusCharacteristic.warningHistoryList.warningCd",
            "creditProfile.telusCharacteristic.warningHistoryList.warningTypeCd",
            "creditProfile.telusCharacteristic.warningHistoryList.warningItemTypeCd",
            "creditProfile.telusCharacteristic.warningHistoryList.warningStatusCd",
            "engagedParty.individualIdentification.identificationId",
            "engagedParty.organizationIdentification.identificationId"
    };

    private ObjectMapper objectMapper;

    private CryptoService cryptoService;

    public AuditLogFilter(ObjectMapper objectMapper, CryptoService cryptoService) {
        this.objectMapper = objectMapper;
        this.cryptoService = cryptoService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    	
    	
    	HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        
        HttpServletRequestWrapper wrappedRequest = new MultiReadHttpServletRequest(httpRequest);
        try {
	        AuditService.resetAuditContext();
	        AuditService.auditContext().setEventTimestamp(Timestamp.from(Instant.now()));
	        if (httpRequest.getContentType() != null && httpRequest.getContentType().toLowerCase().contains(JSON_CONTENT_TYPE) && HttpMethod.PATCH.name().equals(httpRequest.getMethod())) {
	            Map<String, Object> jsonBody = objectMapper.readValue(wrappedRequest.getInputStream(), new TypeReference<Map<String, Object>>() {
	            });
	            encrypt(jsonBody);
	            AuditService.auditContext().setInputRequest(objectMapper.writeValueAsString(jsonBody));
	        }	       
			filterChain.doFilter(wrappedRequest, servletResponse);
		} catch (Throwable e) {
			LOGGER.warn("doFilter failed.",e);
			((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request.");
		} 
    }

    private void encrypt(Map<String, Object> root) {
        for (String securedNode : SECURED_NODES) {
            try {
                encrypt(root, securedNode.split("\\."), 0);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void encrypt(Map<String, Object> root, String[] path, int index) throws Exception {
        if (index >= path.length) {
            return;
        }

        Object o = root.get(path[index]);
        if (o instanceof Map) {
            encrypt((Map<String, Object>) o, path, index + 1);
        } else if (o instanceof Collection) {
            Iterator iterator = ((Collection) o).iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                if (next instanceof Map) {
                    encrypt((Map<String, Object>) next, path, index + 1);
                }
            }
        } else if (o != null && index == path.length - 1) {
            if (!StringUtils.isBlank(o.toString())) {
                root.put(path[index], cryptoService.encrypt(o.toString()));
            }
        }
    }
}
