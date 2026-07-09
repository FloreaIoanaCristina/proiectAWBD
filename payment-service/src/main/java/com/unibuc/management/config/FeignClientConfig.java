package com.unibuc.management.config;

import com.unibuc.management.security.JwtService;
import feign.RequestInterceptor;
import feign.Retryer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final JwtService jwtService;

    @Bean
    public RequestInterceptor jwtRequestInterceptor() {
        return template -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String username;
            List<String> roles;
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                username = authentication.getName();
                roles = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();
            } else {
                username = "service";
                roles = List.of("ROLE_SERVICE");
            }

            template.header("Authorization", "Bearer " + jwtService.generate(username, roles));
        };
    }

    /**
     * Retry mechanism for transient failures (e.g. a callee briefly unavailable):
     * up to 3 attempts with backoff before the circuit breaker fallback kicks in.
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }
}
