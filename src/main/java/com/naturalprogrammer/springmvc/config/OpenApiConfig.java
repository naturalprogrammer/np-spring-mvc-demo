package com.naturalprogrammer.springmvc.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@OpenAPIDefinition(
        info = @Info(
                title = "A sample Spring Boot non-reactive application",
                description = """
                        A model real world Application with user module using Spring Boot non-reactive stack
                         """,
                contact = @Contact(
                        name = "Sanjay Patel",
                        url = "https://www.naturalprogrammer.com",
                        email = "skpatel20@gmail.com"
                ),
                license = @License(
                        name = "Apache 2.0 License",
                        url = "https://github.com/naturalprogrammer/np-spring-mvc-demo/LICENSE.txt")),
        security = {@SecurityRequirement(name = "JWT")}
)
public class OpenApiConfig {
}
