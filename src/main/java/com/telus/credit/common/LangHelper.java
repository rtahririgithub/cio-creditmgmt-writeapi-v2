package com.telus.credit.common;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component
public class LangHelper {
	private static ResourceBundleMessageSource messageSource;

	@Autowired
	public LangHelper(ResourceBundleMessageSource messageSource) {
		LangHelper.messageSource = messageSource;
	}

	/**
	 * Get the locale specific message
	 * @param msgCode
	 * @return
	 */
	public static String getMessage(String msgCode) {
		Locale locale = LocaleContextHolder.getLocale();
		String msg;
		try {
			msg = messageSource.getMessage(msgCode, null, locale);
		} catch (Exception e) {
			msg = "Failed to find message for msgCode: " + msgCode;
		}
		return msg;
	}


	/**
	 * Get the locale specific message passing in the message parameters
	 * @param msgCode
	 * @param params
	 * @return
	 */
	public static String getMessage(String msgCode, Object[] params) {
		Locale locale = LocaleContextHolder.getLocale();
		String msg;
		try {
			msg = messageSource.getMessage(msgCode, params, locale);
		} catch (NoSuchMessageException e) {
			msg = "Failed to find message for msgCode: " + msgCode;
		}
		return msg;
	}

	public static ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

}
