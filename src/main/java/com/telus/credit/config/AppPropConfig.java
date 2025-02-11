package com.telus.credit.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

  
@Configuration
public class  AppPropConfig {

  @Value("${crypto.keystoreUrl}")
  private static String keystoreUrl;

public static String getKeystoreUrl() {
	return keystoreUrl;
}

 
}
