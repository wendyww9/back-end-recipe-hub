package com.recipehub.backendrecipehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF for API testing
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll() // Allow auth endpoints
                        .requestMatchers("/api/users/register", "/api/users/login", "/api/users/logout").permitAll() // Allow user endpoints too
                        .requestMatchers(HttpMethod.GET, "/api/recipes/**").permitAll() // Allow public recipe viewing
                        .requestMatchers("/h2-console/**").permitAll() // Allow H2 console access
                        .anyRequest().authenticated() // Require authentication for all other endpoints
                )
                .httpBasic(Customizer.withDefaults()) // Enable basic authentication
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Logout successful\"}");
                        })
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                )
                .headers(headers -> headers.frameOptions().disable()); // Disable frame options for H2 console

        return http.build();
    }
}