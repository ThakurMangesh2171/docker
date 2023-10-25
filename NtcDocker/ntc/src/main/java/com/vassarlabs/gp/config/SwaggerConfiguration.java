package com.vassarlabs.gp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Value("${swagger.base.url}")
    private String swaggerBaseUrl;

    @Value("${swagger.protocol}")
    private String protocol;

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(metaData())
                .host(swaggerBaseUrl)
                .protocols(new HashSet<>(Arrays.asList(protocol))); // Specify HTTPS/HTTP as the protocol

    }
    private ApiInfo metaData() {
        ApiInfo apiInfo = new ApiInfo(
                "Spring Boot REST API for GP  Application",
                "This is an API Documentation for GP Application",
                "1.0",
                "Terms of service",
                (String) null,null,null);
        return apiInfo;
    }

}