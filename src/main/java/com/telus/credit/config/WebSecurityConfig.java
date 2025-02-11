
package com.telus.credit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	
    	//disable csrf since this is a backend API.
    	 http.csrf().disable();
    	
		//Add httpStrictTransportSecurity header
		http.headers()
            .httpStrictTransportSecurity()
               // .includeSubDomains(true)
                .maxAgeInSeconds(31536001);

    
    
    }
}


