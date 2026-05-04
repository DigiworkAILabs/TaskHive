package com.digiwork.taskhive.common.config;

import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CorsConfigurationSource corsConfigurationSource;
        private final com.digiwork.taskhive.module.auth.security.CsrfCookieFilter csrfCookieFilter;

        private static final String[] PUBLIC_ENDPOINTS = {
                        "/api/v1/auth/login",
                        "/api/v1/auth/logout",
                        "/api/v1/auth/activate-account",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/reset-password",
                        "/api/v1/auth/refresh",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/ws/**"
        };

        // Kubernetes liveness/readiness probes must remain unauthenticated
        private static final String[] ACTUATOR_PROBE_ENDPOINTS = {
                        "/actuator/health/liveness",
                        "/actuator/health/readiness"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler requestHandler = new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler();
                requestHandler.setCsrfRequestAttributeName("_csrf");

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(requestHandler)
                                                .ignoringRequestMatchers(PUBLIC_ENDPOINTS)
                                                .ignoringRequestMatchers(ACTUATOR_PROBE_ENDPOINTS)
                                )

                                // ── Security Headers (NFR-SEC-14) ────────────────────────────────
                                .headers(headers -> headers
                                                // X-Frame-Options: DENY — prevents clickjacking
                                                .frameOptions(frame -> frame.deny())
                                                // Content-Security-Policy — restricts allowed content sources
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; " +
                                                                                                "script-src 'self'; " +
                                                                                                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                                                                                                +
                                                                                                "font-src 'self' https://fonts.gstatic.com; "
                                                                                                +
                                                                                                "img-src 'self' data: blob:; "
                                                                                                +
                                                                                                "connect-src 'self' ws: wss:; "
                                                                                                +
                                                                                                "frame-ancestors 'none'"))
                                                // Strict-Transport-Security — HSTS, browsers connect via HTTPS only for
                                                // 1 year
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000))
                                                // Referrer-Policy — limit referrer info passed to third parties
                                                .referrerPolicy(referrer -> referrer
                                                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers(ACTUATOR_PROBE_ENDPOINTS).permitAll()
                                                .requestMatchers("/actuator/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .addFilterAfter(csrfCookieFilter, org.springframework.security.web.authentication.www.BasicAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }
}
