package com.naturalprogrammer.springmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@ConfigurationPropertiesScan
public class NpSpringMvcDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NpSpringMvcDemoApplication.class, args);
    }

}
