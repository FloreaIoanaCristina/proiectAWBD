package com.unibuc.management.config;

import com.unibuc.management.security.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final DataSource dataSource;
    //private final UserDetailsService userDetailsService;
    public SecurityConfig(DataSource dataSource/*,UserDetailsService userDetailsService*/) {
        this.dataSource = dataSource;
        //this.userDetailsService = userDetailsService;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,UserDetailsService userDetailsService) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository())
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers("/auth/**", "/h2-console/**")
                )
//                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/auth/**",
                                "/api/medical-services/**",
                                "/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
//                .formLogin(form -> form
//                        .loginProcessingUrl("/auth/login")
//                        .successHandler((request, response, authentication) -> {
//                            response.setStatus(200);
//                            response.setContentType("application/json");
//                            response.getWriter().write("{\"message\":\"Login successful\",\"username\":\"" + authentication.getName() + "\"}");
//                        })
//                        .failureHandler((request, response, exception) -> {
//                            response.setStatus(401);
//                            response.setContentType("application/json");
//                            response.getWriter().write("{\"error\":\"Bad credentials\"}");
//                        })
//                        .permitAll()
//                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .deleteCookies("JSESSIONID", "remember-me")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.getWriter().write("{\"message\":\"Logout successful\"}");
                        })
                )
                .rememberMe(remember -> remember
                        .userDetailsService(userDetailsService)
                        .key("SecretKeyClinicaManagement2026")
                        .tokenValiditySeconds(86400 * 7)
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Forbidden\"}");
                        })
                )
                .sessionManagement(session ->session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                )
                .addFilterAfter((request, response, chain) -> {
                    jakarta.servlet.http.HttpServletResponse httpResponse = (jakarta.servlet.http.HttpServletResponse) response;

                    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                    if (csrfToken != null) {
                        httpResponse.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
                    }
                    chain.doFilter(request, response);
                }, CsrfFilter.class);

        return http.build();
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
//
//        userDetailsManager.setUsersByUsernameQuery(
//                "SELECT username, password, 1 as enabled FROM [USERS] WHERE username = ?"
//        );
//
//        userDetailsManager.setAuthoritiesByUsernameQuery(
//                "SELECT username, role as authority FROM [USERS] WHERE username = ?"
//        );
//
//        return userDetailsManager;
//    }

    @Bean
    public UserDetailsService userDetailsService(JdbcTemplate jdbcTemplate) {
        return username -> {
            String sql = "SELECT id, username, password, role FROM [USERS] WHERE username = ?";

            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                        Long id = rs.getLong("id");
                        String dbUsername = rs.getString("username");
                        String dbPassword = rs.getString("password");
                        String dbRole = rs.getString("role");

                        String authorityWithPrefix = dbRole.startsWith("ROLE_") ? dbRole : "ROLE_" + dbRole;

                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority(authorityWithPrefix)
                        );

                        return new CustomUserDetails(
                                id,
                                dbUsername,
                                dbPassword,
                                Collections.singletonList(new SimpleGrantedAuthority(authorityWithPrefix))
                        );
                    }, username)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionSecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("X-XSRF-TOKEN","Cache-Control", "Content-Type", "Authorization", "Accept"));
        configuration.setExposedHeaders(Collections.singletonList("X-CSRF-TOKEN"));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}