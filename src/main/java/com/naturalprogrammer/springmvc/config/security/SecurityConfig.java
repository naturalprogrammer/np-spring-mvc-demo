package com.naturalprogrammer.springmvc.config.security;

import com.naturalprogrammer.springmvc.config.MyProperties;
import com.naturalprogrammer.springmvc.config.sociallogin.*;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.naturalprogrammer.springmvc.common.Path.*;
import static com.naturalprogrammer.springmvc.user.features.login.AuthScope.*;
import static jakarta.servlet.DispatcherType.ERROR;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAuthority;
import static org.springframework.security.authorization.AuthorizationManagers.allOf;

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
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .securityContext(customizer -> customizer.securityContextRepository(new NullSecurityContextRepository()))
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(configurer -> configurer.jwt(customizer -> customizer
                        .decoder(jwtDecoder(properties))
                        .jwtAuthenticationConverter(new JwtAuthenticationConverter(userRepository))
                ))
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
                                .requestMatchers(GET, USERS).access(isAdmin())
                                .requestMatchers(POST, LOGIN).permitAll()
                                .requestMatchers(POST, FORGOT_PASSWORD).permitAll()
                                .requestMatchers(POST, RESET_PASSWORD).permitAll()
                                .requestMatchers(PATCH, USERS + "/*/display-name").hasAuthority(NORMAL.scope())
                                .requestMatchers(POST, USERS + "/*/verifications").hasAuthority(NORMAL.scope())
                                .requestMatchers(PUT, USERS + "/*/verifications").hasAuthority(NORMAL.scope())
                                .requestMatchers(GET, USERS + "/*/access-token").hasAuthority(AUTH_TOKENS.scope())
                                .requestMatchers(GET, USERS + "/*/auth-tokens").hasAuthority(AUTH_TOKENS.scope())
                                .requestMatchers(POST, USERS + "/*/exchange-resource-token").hasAuthority(EXCHANGE_RESOURCE_TOKEN.scope())
                                .requestMatchers(GET, USERS + "/*").hasAuthority(NORMAL.scope())
                                .requestMatchers(PATCH, USER + "/password").hasAuthority(NORMAL.scope())
                                .requestMatchers(POST, USER + "/email-change-request").hasAuthority(NORMAL.scope())
                                .requestMatchers(PATCH, USER + "/email-change-request").hasAuthority(NORMAL.scope())
                                .requestMatchers(GET,
                                        "/",
                                        "/context",
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

    private static AuthorizationManager<RequestAuthorizationContext> isAdmin() {
        return allOf(
                hasAuthority(NORMAL.scope()),
                hasAuthority((Role.ADMIN.authority())),
                hasAuthority(Role.VERIFIED.authority()));
    }
}
