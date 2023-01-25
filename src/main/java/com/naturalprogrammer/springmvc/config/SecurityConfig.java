package com.naturalprogrammer.springmvc.config;

import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import static com.naturalprogrammer.springmvc.common.Path.USERS;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final MyProperties properties;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .cors().and()
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(configurer -> configurer.jwt()
                        .decoder(jwtDecoder(properties))
                        .jwtAuthenticationConverter(new JwtAuthenticationConverter(userRepository))
                )
                .authorizeHttpRequests(config ->
                        config
                                .requestMatchers(HttpMethod.POST, USERS).permitAll()
                                .requestMatchers(HttpMethod.PATCH, USERS + "/*/display-name").authenticated()
                                .requestMatchers(HttpMethod.GET,
                                        "/v3/api-docs/**",
                                        "/favicon.ico",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**"
                                ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/error").permitAll()
                                .anyRequest().denyAll()
                ).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private JwtDecoder jwtDecoder(MyProperties properties) {
        return NimbusJwtDecoder.withPublicKey(properties.jws().publicKey()).build();
    }
}
