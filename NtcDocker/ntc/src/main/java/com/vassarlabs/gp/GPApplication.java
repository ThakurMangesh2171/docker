package com.vassarlabs.gp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class GPApplication {

	public static void main(String[] args) {
		SpringApplication.run(GPApplication.class, args);
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		// Do any additional configuration here
		return builder.build();
	}
	
	@Bean
	public PersistenceExceptionTranslator exceptionTranslator() {
		return new PersistenceExceptionTranslator() {
			@Override
			public DataAccessException translateExceptionIfPossible(RuntimeException e) {
				if (e instanceof DataAccessException) {
					return (DataAccessException) e;
				}
				if (e.getCause() instanceof DataAccessException) {
					return (DataAccessException) e.getCause();
				}
				return null;
			}
		};
	}


}
