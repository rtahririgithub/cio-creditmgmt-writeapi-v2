package com.telus.credit.crypto.service;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.telus.credit.config.JceCryptoImplLocal;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.framework.crypto.EncryptionUtil;


@Service
public class CryptoService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CryptoService.class);

  private JceCryptoImplLocal crypto;

  public CryptoService(JceCryptoImplLocal jceCryptoImpl) {
    this.crypto = jceCryptoImpl;
  }

  public String encrypt(String data) throws Exception {
    Assert.isTrue(StringUtils.isNotBlank(data), "Invalid input data=" + data);
   String encryptedVal = null;
   try {
	encryptedVal= new String(crypto.encrypt(data.getBytes(StandardCharsets.UTF_8)));
} catch (Exception e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
    return encryptedVal;
  }

  public String encryptOrNull(String data) throws Exception {
    return StringUtils.isBlank(data) ? null : encrypt(data);
  }

  public String decrypt(String data) throws Exception {
    Assert.isTrue(StringUtils.isNotBlank(data), "Invalid input data=" + data);
    //jceCryptoImplDecryptedVal
    String decryptedVal=null;    
    try {
		decryptedVal = new String(crypto.decrypt(data.getBytes(StandardCharsets.UTF_8)));
	} catch (Exception e) {
	    //pilotDecryptedVal
	    decryptedVal = EncryptionUtil.decrypt(data);
	}
    return decryptedVal;
  }

  public String decryptAndIgnoreError(String data) {
    try {
      return StringUtils.trimToNull(data) != null ? decrypt(data) : null;
    } catch (Exception e) {
        LOGGER.error("{}: Error decrypting string len({}) . {}",  ExceptionConstants.STACKDRIVER_METRIC,data.length() , ExceptionHelper.getStackTrace(e));
      return data;
    }
  }

  public String decryptAndNullOnError(String data) {
    try {
      return StringUtils.trimToNull(data) != null ? decrypt(data) : null;
    } catch (Exception e) {
      LOGGER.debug("Error decrypting string len({}) {}", data.length() , ExceptionHelper.getStackTrace(e));
      return null;
    }
  }
}
