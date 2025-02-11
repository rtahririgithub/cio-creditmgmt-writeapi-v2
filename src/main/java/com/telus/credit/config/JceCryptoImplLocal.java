 package com.telus.credit.config;
 
 import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.framework.crypto.CryptoFixedKey;
import com.telus.framework.crypto.impl.jce.AlgorithmParamSpecGenerator;
 
 
 public class JceCryptoImplLocal
   implements CryptoFixedKey
 {
  private static Log logger = LogFactory.getLog(JceCryptoImplLocal.class);
   
   private String m_keystoreURL;
   private String m_keystoreType = "JCEKS";
   
   private String m_keystorePassword;
   private String m_cipherTransformation = "AES/CBC/PKCS5Padding";
   private String m_cipherJceProvider = "SunJCE";
 
   
   private String m_keyAlias;
   
   private String m_keyPassword;
   
   private Key m_key;
   
   private KeyStore m_keyStore;
   
   private AlgorithmParamSpecGenerator m_algorithmParamSpecGenerator;
   
   private boolean encodeBase64 = false;
   
   private Base64 m_base64Encoder;
 
   
   public JceCryptoImplLocal() {}
 
   
   public JceCryptoImplLocal(boolean encodeBase64) {
	   this.encodeBase64 = encodeBase64;
   }
 
 
 
   
   public void init(byte[] keystore_content) throws Exception {
	this.m_keyStore = KeyStore.getInstance(this.m_keystoreType);

	//convert bytes to inputstream 
	InputStream byteArrayInputStream = new java.io.ByteArrayInputStream(keystore_content);  
    this.m_keyStore.load(byteArrayInputStream, (this.m_keystorePassword == null) ? null : this.m_keystorePassword.toCharArray());
   
   if (this.m_keyPassword == null) {
	   logger.error("keyPassword cannot be null." + ExceptionConstants.STACKDRIVER_METRIC);
   		this.m_key = this.m_keyStore.getKey(this.m_keyAlias, new char[0]);
   } else {
	   this.m_key = this.m_keyStore.getKey(this.m_keyAlias, this.m_keyPassword.toCharArray());
    } 
if (this.encodeBase64) 
		this.m_base64Encoder = new Base64();
  
  } 
 
   
   
   public void setKeystoreURL(String keystoreURL) {
     this.m_keystoreURL = keystoreURL;
   }
 
 
   
   public void setKeystoreType(String keystoreType) {
     this.m_keystoreType = keystoreType;
   }
 
 
   
   public void setKeystorePassword(String keystorePassword) {
     this.m_keystorePassword = keystorePassword;
   }
 
 
   
   public void setKeyAlias(String keyAlias) {
     this.m_keyAlias = keyAlias;
   }
 
 
   
   public void setKeyPassword(String keyPassword) {
     this.m_keyPassword = keyPassword;
   }
 
 
   
   public void setCipherTransformation(String transformation) {
    this.m_cipherTransformation = transformation;
   }
 
 
   
   public void setCipherJceProvider(String jceProvider) {
    this.m_cipherJceProvider = jceProvider;
   }
 
 
   
   public void setAlgorithmParamSpecGenerator(AlgorithmParamSpecGenerator algorithmParamSpecGenerator) {
     this.m_algorithmParamSpecGenerator = algorithmParamSpecGenerator;
   }
 
 
   
   public void setEncodeBase64(boolean encodeBase64) {
     this.encodeBase64 = encodeBase64;
   }
 
 
 
 
 
 
 
 
   
   public byte[] encrypt(byte[] clearData) throws Exception {
     Cipher cipher = Cipher.getInstance(this.m_cipherTransformation, this.m_cipherJceProvider);
     AlgorithmParameterSpec paramSpec = null;
     
     if (this.m_algorithmParamSpecGenerator != null) {
       paramSpec = this.m_algorithmParamSpecGenerator.generateParamSpec();
       cipher.init(1, this.m_key, paramSpec);
     } else {
       cipher.init(1, this.m_key);
     } 
 
     
     int outputLength = cipher.getOutputSize(clearData.length);
     
     byte[] cipherData = new byte[outputLength];
     cipher.doFinal(clearData, 0, clearData.length, cipherData, 0);
     
     if (this.m_algorithmParamSpecGenerator != null) {
       cipherData = this.m_algorithmParamSpecGenerator.mixCipherAndParamSpec(cipherData, paramSpec);
     }
    if (this.encodeBase64) {
       return this.m_base64Encoder.encode(cipherData);
     }
     return cipherData;
   }
 
 
   
   public byte[] decrypt(byte[] cipherData) throws Exception {
     byte[] data;
     if (this.encodeBase64) {
       data = this.m_base64Encoder.decode(cipherData);
     } else {
       data = cipherData;
     } 
     Cipher cipher = Cipher.getInstance(this.m_cipherTransformation, this.m_cipherJceProvider);
     
     AlgorithmParameterSpec paramSpec = null;
     if (this.m_algorithmParamSpecGenerator != null) {
       paramSpec = this.m_algorithmParamSpecGenerator.retrieveParamSpecFromCipher(data);
       cipher.init(2, this.m_key, paramSpec);
     } else {
      cipher.init(2, this.m_key);
     } 
 
 
     
     if (this.m_algorithmParamSpecGenerator != null) {
      data = this.m_algorithmParamSpecGenerator.retrieveDataFromCipher(data);
     }
     return cipher.doFinal(data, 0, data.length);
   }
 
 
 
   
   public byte[] hash(byte[] clear) {
     return null;
   }
 }
