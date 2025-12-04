package com.emobile.springtodo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Task CRUD API")
                        .version("1.0.0")
                        .description("REST API для управления списком дел. "
                                + "Поддерживает CRUD операции с задачами.")
                        .contact(new Contact()
                                .name("Ravil Sultanov")
                                .email("xxx@yandex.ru")
                                .url("https://localhost:8080")));
    }
}