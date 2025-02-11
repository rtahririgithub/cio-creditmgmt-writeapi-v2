package com.telus.credit;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.telus.credit.config.TracerConfig;

@SpringBootApplication
@EnableRetry
@EnableTransactionManagement
public class CreditProfileApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreditProfileApplication.class);

	
	public static void main(String[] args) throws IOException {
		
		ConfigurableApplicationContext ctx = SpringApplication.run(CreditProfileApplication.class, args);
		System.out.println("CreditProfileApplication started");
		try {	
			String str = ctx.getEnvironment().getProperty("spring.datasource.url");
			LOGGER.info("spring.datasource.url=" + str);
			
		} catch (Exception e) {
			LOGGER.warn("Couldn't get env details {}", e.getMessage());
		}		
		try {		
			TracerConfig.createAndRegisterWithGCP();
			LOGGER.info("Tracer init done");
		} catch (Exception e) {
			LOGGER.warn("Couldn't init Tracer {}", e.getMessage());
		}
			
		Properties prop = new Properties();
		try {
			prop.load(CreditProfileApplication.class.getClassLoader().getResourceAsStream("git.properties"));
			LOGGER.info("Git information: {}", prop);

		} catch (Exception e) {
			LOGGER.warn("Couldn't load git information {}", e.getMessage());
		}
	}

}
