package com.skishop.frontend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Spring Security Configuration
 * Configures authentication and authorization settings.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.skishop.auth.enabled:true}")
    private boolean authEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (!authEnabled) {
            // Authentication disabled mode (for local development)
            http
                .authorizeHttpRequests(authz -> authz
                    .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable);
        } else {
            // Authentication enabled mode (for production)
            http
                .authorizeHttpRequests(authz -> authz
                    // Public resources
                    .requestMatchers(
                        "/", "/home", "/products/**", "/search/**", 
                        "/api/public/**", "/webjars/**", "/css/**", 
                        "/js/**", "/images/**", "/favicon.ico"
                    ).permitAll()
                    // Admin only
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    // All other requests require authentication
                    .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .defaultSuccessUrl("/dashboard", true)
                    .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true)
                )
                .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers("/api/webhook/**")
                )
                .sessionManagement(session -> session
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                );
        }
        
        return http.build();
    }
}
