package com.desafio.fullstack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Desafio Full-Stack API")
                .version("1.0.0")
                .description("Gerenciamento de Empresas e Fornecedores")
                .contact(new Contact()
                    .name("Arthur")
                    .email("arthurassilva99@gmail.com"))
            );
    }
}
