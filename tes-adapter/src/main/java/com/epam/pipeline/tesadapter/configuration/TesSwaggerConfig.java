package com.epam.pipeline.tesadapter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableSwagger2
public class TesSwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.epam.pipeline.tesadapter.controller"))
                .paths(PathSelectors.ant("/v1/tasks/**"))
                .build();
    }


    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title("TES adapter API Documentation")
                .description("Simple TES adapter application. " +
                        "Without Security Configuration and Context")
                .version("1.0.0")
                .build();
    }

    @Bean
    public HttpMessageConverter httpMessageConverter(){
        return new MappingJackson2HttpMessageConverter (  );
    }
}
