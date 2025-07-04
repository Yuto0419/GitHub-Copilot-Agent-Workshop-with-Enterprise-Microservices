package com.skishop.user.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    /**
     * Password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Security filter chain when authentication is enabled
     */
    @Bean
    @ConditionalOnProperty(name = "skishop.authfunc.enable", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain securityFilterChainWithAuth(HttpSecurity http) throws Exception {
        log.info("Security Configuration: Authentication ENABLED");
        
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/health").permitAll()
                        // User registration endpoints don't require authentication
                        .requestMatchers("/users/register").permitAll()
                        .requestMatchers("/users/verify-email").permitAll()
                        .requestMatchers("/users/forgot-password").permitAll()
                        .requestMatchers("/users/reset-password").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                            // Add JWT configuration as needed
                        }))
                .build();
    }

    /**
     * Security filter chain when authentication is disabled
     */
    @Bean
    @ConditionalOnProperty(name = "skishop.authfunc.enable", havingValue = "false")
    public SecurityFilterChain securityFilterChainWithoutAuth(HttpSecurity http) throws Exception {
        log.info("Security Configuration: Authentication DISABLED (Development Mode)");
        
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Allow all endpoints (for development)
                        .anyRequest().permitAll())
                .build();
    }
}
