package com.naturalprogrammer.springmvc.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

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
                        url = "https://github.com/naturalprogrammer/np-spring-mvc-demo/LICENSE.txt"))
)
public class OpenApiConfig {
}
