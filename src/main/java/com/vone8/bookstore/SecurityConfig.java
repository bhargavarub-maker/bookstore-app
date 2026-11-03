package com.vone8.bookstore;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.time.Instant;

/**
 * HTTP Basic authentication: every call to /v1/** must send
 * "Authorization: Basic base64(username:password)". The user and password come from
 * application.properties (spring.security.user.*). Web pages, Swagger UI and the H2
 * console stay public; the data behind them does not.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/**").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(basic -> basic.authenticationEntryPoint(SecurityConfig::unauthorized))
                // No cookies or sessions: the client sends the credentials on every request,
                // so there is nothing for a CSRF attack to ride on.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                // The H2 console draws itself in frames
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    /** Answers a missing or wrong password with 401 and the same JSON error shape as the rest of the API. */
    private static void unauthorized(HttpServletRequest request, HttpServletResponse response,
                                     AuthenticationException e) throws IOException {
        // This header makes browsers show their own login box. Our web pages have a sign-in
        // form instead, so they send "X-Requested-With: XMLHttpRequest" and get a 401 without it.
        if (!"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Bookstore\"");
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String path = request.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("""
                {"timestamp":"%s","status":401,"error":"Unauthorized",\
                "message":"Send a valid username and password (HTTP Basic authentication)","path":"%s"}"""
                .formatted(Instant.now(), path));
    }
}
