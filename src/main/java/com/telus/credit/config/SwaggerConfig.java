package com.telus.credit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          .apis(RequestHandlerSelectors.basePackage("com.telus.credit"))              
          .paths(PathSelectors.any())                          
          .build()
		  .useDefaultResponseMessages(false)
		  .apiInfo(apiInfo());                                        
    }
	
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("Telus Customer Credit Profile API")
				.description("These APIs are specific to the Telus Customer Credit Profile.")
				.version("1.0")
				.build();

	}
}
