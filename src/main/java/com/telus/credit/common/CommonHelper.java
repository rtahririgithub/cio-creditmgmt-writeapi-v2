package com.telus.credit.common;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telus.credit.dao.entity.CreditProfileEntity;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.model.TelusCreditProfile;

public class CommonHelper {

   private static final Logger LOGGER = LoggerFactory.getLogger(CommonHelper.class);

   public static <T> Collection<T> nullSafe(Collection<T> collection) {
      return collection == null ? Collections.emptyList() : collection;
   }

   public static <T> Object safeTransform(T input, Function<T, Object> supplier, Object defaultValue, String logMsg) {
      if (ObjectUtils.isEmpty(input)) {
         if (logMsg != null) {
            LOGGER.debug("{} use default {}", logMsg, defaultValue);
         }
         return defaultValue;
      }

      try {
         return supplier.apply(input);
      } catch (Exception e) {
         LOGGER.warn(" Error parsing input [ {}] , use default value. {}",   input, ExceptionHelper.getStackTrace(e));
         return defaultValue;
      }
   }
   
   
   public static void refineCreditProgram(TelusCreditProfile characteristic) {
       if (characteristic == null) {
           LOGGER.info("Characteristic is null, no refinement needed");
           return;
       }

       String creditProgramName = StringUtils.trimToEmpty(characteristic.getCreditProgramName());
       switch (creditProgramName.toLowerCase()) {
           case "clp":
               characteristic.setAverageSecurityDepositAmt(null);
               break;
           case "ndp":
               characteristic.setAverageSecurityDepositAmt(null);
               characteristic.setClpCreditLimitAmt(null);
               characteristic.setClpRatePlanAmt(null);
               characteristic.setClpContractTerm(null);
               break;
           case "dep":
               characteristic.setClpCreditLimitAmt(null);
               characteristic.setClpRatePlanAmt(null);
               characteristic.setClpContractTerm(null);
               break;
           default:
               LOGGER.info("Ignore refining credit program: {}", characteristic.getCreditProgramName());
               break;
       }
   }

	public static void refineCreditProgram(CreditProfileEntity entitiy) {
	    if (entitiy == null) {
	        LOGGER.info("Characteristic is null, no refinement needed");
	        return;
	    }
	
	    String creditProgramName = StringUtils.trimToEmpty(entitiy.getCreditProgramName());
	    switch (creditProgramName.toLowerCase()) {
	        case "clp":
	            entitiy.setSecurityDepAmt(null);
	            break;
	        case "ndp":
	            entitiy.setSecurityDepAmt(null);
	            entitiy.setClpCreditLimitAmt(null);
	            entitiy.setClpRatePlanAmt(null);
	            entitiy.setClpContractTerm(null);
	            break;
	        case "dep":
	            entitiy.setClpCreditLimitAmt(null);
	            entitiy.setClpRatePlanAmt(null);
	            entitiy.setClpContractTerm(null);
	            break;
	        default:
	            LOGGER.info("Ignore refining credit program: {}", entitiy.getCreditProgramName());
	            break;
	    }
	}   
}
