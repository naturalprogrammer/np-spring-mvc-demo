package com.naturalprogrammer.springmvc.config;

import com.naturalprogrammer.springmvc.config.sociallogin.*;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.naturalprogrammer.springmvc.common.Path.LOGIN;
import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.user.features.login.AuthScope.*;
import static jakarta.servlet.DispatcherType.ERROR;
import static org.springframework.http.HttpMethod.*;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final MyProperties properties;
    private final UserRepository userRepository;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final MyOidcUserService oidcUserService;
    private final MyOAuth2UserService oauth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .cors().and()
                .csrf(AbstractHttpConfigurer::disable)
                .securityContext(customizer -> customizer.securityContextRepository(new NullSecurityContextRepository()))
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(configurer -> configurer.jwt()
                        .decoder(jwtDecoder(properties))
                        .jwtAuthenticationConverter(new JwtAuthenticationConverter(userRepository))
                )
                .oauth2Login(oauth2Login -> {
                    oauth2Login.authorizationEndpoint(endpoint ->
                            endpoint.authorizationRequestRepository(new HttpCookieOAuth2AuthorizationRequestRepository())
                    );
                    oauth2Login.userInfoEndpoint(userInfoEndpointConfig -> {
                        userInfoEndpointConfig.oidcUserService(oidcUserService);
                        userInfoEndpointConfig.userService(oauth2UserService);
                    });
                    oauth2Login.successHandler(oAuth2AuthenticationSuccessHandler);
                    oauth2Login.failureHandler(new OAuth2AuthenticationFailureHandler());
                })
                .authorizeHttpRequests(config ->
                        config
                                .requestMatchers(POST, USERS).permitAll()
                                .requestMatchers(POST, LOGIN).permitAll()
                                .requestMatchers(PATCH, USERS + "/*/display-name").hasAuthority(NORMAL.scope())
                                .requestMatchers(POST, USERS + "/*/verification").hasAuthority(NORMAL.scope())
                                .requestMatchers(GET, USERS + "/*/access-token").hasAuthority(RESOURCE_TOKEN.scope())
                                .requestMatchers(GET, USERS + "/*/resource-token").hasAuthority(RESOURCE_TOKEN.scope())
                                .requestMatchers(POST, USERS + "/*/exchange-resource-token").hasAuthority(EXCHANGE_RESOURCE_TOKEN.scope())
                                .requestMatchers(GET, USERS + "/*").hasAuthority(NORMAL.scope())
                                .requestMatchers(GET,
                                        "/",
                                        "/index.html",
                                        "/oauth2/authorization/google",
                                        "/login/oauth2/code/google",
                                        "/v3/api-docs/**",
                                        "/favicon.ico",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/webjars/**"
                                ).permitAll()
                                .dispatcherTypeMatchers(ERROR).permitAll()
                                .anyRequest().denyAll()
                ).build();
    }

    private JwtDecoder jwtDecoder(MyProperties properties) {
        return NimbusJwtDecoder.withPublicKey(properties.jws().publicKey()).build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins(properties.homepage());
            }
        };
    }
}
