package com.library.catalogue.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI libraryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Catalogue API")
                        .version("1.0")
                        .description("RESTful API для каталогу бібліотеки")
                        .contact(new Contact()
                                .name("Yana Dumanska & Nadiia Zinchuk")));
    }
}
