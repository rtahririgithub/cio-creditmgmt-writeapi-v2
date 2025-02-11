package com.telus.credit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@ConfigurationProperties("credit.pubsub.connector")
public class CreditPubSubConfig {
	private static final Duration TIME_OUT = Duration.ofMillis(15000);
	private String pubSubURL;
	private String username;
	private String password;

	public String getPubSubURL() {
		return pubSubURL;
	}

	public void setPubSubURL(String pubSubURL) {
		this.pubSubURL = pubSubURL;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplateBuilder()
				.setConnectTimeout(TIME_OUT)
				.setReadTimeout(TIME_OUT)
				.build();
	}


}
