package com.skishop.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * API Gateway Application
 * 
 * <p>Unified access point to ski shop microservices</p>
 * 
 * <h3>Main features:</h3>
 * <ul>
 *   <li>Request routing - Dynamic routing to each microservice</li>
 *   <li>Authentication & Authorization - Unified authentication with JWT and OAuth2</li>
 *   <li>Rate limiting - Redis-based rate limiting</li>
 *   <li>Circuit breaker - Fallback processing during failures</li>
 *   <li>Logging & Metrics - Distributed tracing and monitoring</li>
 * </ul>
 * 
 * <p>Configuration management and route definition using the latest features of Java 21</p>
 * 
 * @since 1.0.0
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Custom route configuration
     * 
     * <p>Dynamic route configuration using Java 21's var type inference and Text Blocks</p>
     * <p>In addition to the default application.yml route configuration,
     * defines dynamic routing and filtering logic</p>
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // User management service - Authentication required, high rate limit
            .route("user-service", r -> r.path("/api/users/**")
                .filters(f -> f
                    .circuitBreaker(c -> c.setName("user-service")
                        .setFallbackUri("forward:/fallback/user"))
                    .retry(retryConfig -> retryConfig.setRetries(3))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("${app.services.user-management:http://localhost:8081}"))
            
            // Authentication service - Highest priority, high availability
            .route("auth-service", r -> r.path("/api/auth/**")
                .filters(f -> f
                    .circuitBreaker(c -> c.setName("auth-service")
                        .setFallbackUri("forward:/fallback/auth"))
                    .retry(retryConfig -> retryConfig.setRetries(2)))
                .uri("${app.services.authentication:http://localhost:8080}"))
            
            // Inventory management service - Core product data
            .route("inventory-service", r -> r.path("/api/products/**", "/api/inventory/**")
                .filters(f -> f
                    .circuitBreaker(c -> c.setName("inventory-service")
                        .setFallbackUri("forward:/fallback/inventory"))
                    .retry(retryConfig -> retryConfig.setRetries(3)))
                .uri("${app.services.inventory-management:http://localhost:8082}"))
            
            // Sales management service - Business critical
            .route("sales-service", r -> r.path("/api/orders/**", "/api/reports/**")
                .filters(f -> f
                    .circuitBreaker(c -> c.setName("sales-service")
                        .setFallbackUri("forward:/fallback/sales"))
                    .retry(retryConfig -> retryConfig.setRetries(3)))
                .uri("${app.services.sales-management:http://localhost:8083}"))
            
            // Payment and cart service - Security important
            .route("payment-cart-service", r -> r.path("/api/cart/**", "/api/payments/**")
                .filters(f -> f
                    .circuitBreaker(c -> c.setName("payment-cart-service")
                        .setFallbackUri("forward:/fallback/payment"))
                    .retry(retryConfig -> retryConfig.setRetries(2)))
                .uri("${app.services.payment-cart:http://localhost:8084}"))
            
            // Point service
            .route("point-service", r -> r.path("/api/points/**")
                .filters(f -> f
                    .circuitBreaker(c -> c.setName("point-service")
                        .setFallbackUri("forward:/fallback/point")))
                .uri("${app.services.point:http://localhost:8085}"))
            
            // Coupon service
            .route("coupon-service", r -> r.path("/api/coupons/**")
                .filters(f -> f
                    .circuitBreaker(c -> c.setName("coupon-service")
                        .setFallbackUri("forward:/fallback/coupon")))
                .uri("${app.services.coupon:http://localhost:8086}"))
            
            // AI support service - AI functionality, rate limiting required
            .route("ai-support-service", r -> r.path("/api/recommendations/**", "/api/search/**", "/api/chat/**", "/api/analytics/**")
                .filters(f -> f
                    .circuitBreaker(c -> c.setName("ai-support-service")
                        .setFallbackUri("forward:/fallback/ai"))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("${app.services.ai-support:http://localhost:8087}"))
            
            .build();
    }

    /**
     * Redis rate limiting configuration
     * 
     * <p>Configuration example using Java 21 record types (planned for future expansion)</p>
     * <p>10 requests/second, burst of 20, replenished at 1-second intervals</p>
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // Consider using record types in future versions of Java 21
        return new RedisRateLimiter(10, 20, 1);
    }

    /**
     * User key resolver configuration
     * 
     * <p>Using Java 21 var type inference, switch expressions, and pattern matching</p>
     * <p>Extracts user ID from authentication token for use as rate limit key</p>
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            var headers = exchange.getRequest().getHeaders();
            var userIdHeader = headers.getFirst("X-User-ID");
            
            // Combination of Java 21's null-safe check and switch expression
            return switch (userIdHeader) {
                case String userId when !userId.isBlank() -> Mono.just(userId);
                case null, default -> 
                    exchange.getPrincipal()
                        .cast(JwtAuthenticationToken.class)
                        .map(JwtAuthenticationToken::getToken)
                        .map(jwt -> jwt.getClaimAsString("sub"))
                        .switchIfEmpty(Mono.just("anonymous"));
            };
        };
    }
}
