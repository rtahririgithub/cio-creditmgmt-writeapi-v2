package com.telus.credit.common;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class RequestContext {

   private String ipAddr;
   private String acceptLang;
   private String corrId;

   public RequestContext(HttpServletRequest request) {
      this.ipAddr = request.getRemoteAddr();
      String lang = request.getHeader(CreditMgmtCommonConstants.HEADER_ACCEPT_LANG);
      if (StringUtils.isBlank(lang)) lang = "en";
      // Sometimes accept-language is coming in as "acceptLang" : "en-US,en;q=0.9"
      // Example: en-GB,en-US;q=0.9,en;q=0.8,fr;q=0.7
      if (lang != null && lang.toLowerCase().startsWith("fr")) {
         lang = "fr";
      } else {
         lang = "en";
      }
      this.acceptLang = lang;
      
      // Get correlation Id
      Object o = request.getAttribute(CreditMgmtCommonConstants.HEADER_CORR_ID);
      if (o != null) {
         this.corrId = (String) o;
      } else {
         this.corrId = UUID.randomUUID().toString();
      }
   }

   /**
    *
    * @param lang
    * @param correlationId
    * @param ip
    */
   public RequestContext(String lang, String correlationId, String ip) {
      this.acceptLang = lang;
      this.ipAddr = ip;
      this.corrId = correlationId;
   }

   public String getIpAddr() {
      return ipAddr;
   }

   public void setIpAddr(String ipAddr) {
      this.ipAddr = ipAddr;
   }

   public String getAcceptLang() {
      return acceptLang;
   }

   public void setAcceptLang(String acceptLang) {
      this.acceptLang = acceptLang;
   }

   public String getCorrId() {
      return corrId;
   }

   public void setCorrId(String corrId) {
      this.corrId = corrId;
   }
}
