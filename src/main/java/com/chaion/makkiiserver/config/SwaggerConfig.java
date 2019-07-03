package com.chaion.makkiiserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
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
                .apis(RequestHandlerSelectors.basePackage("com.chaion.makkiiserver.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiEndpoints());
    }

    private ApiInfo apiEndpoints() {
        return new ApiInfoBuilder().title("Makkii Server REST APIs")
                .description("Version APIs, Coin market APIs, Token Crawler APIs")
                .contact(new Contact("Chaion Makkii Team",
                        "https://chaion.net/",
                        "leo.ren@aion.network, chen@aion.network"))
                .version("1.0.0")
                .build();
    }

}
