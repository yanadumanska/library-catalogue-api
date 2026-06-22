package com.library.catalogue.config;

import com.library.catalogue.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Public endpoints
                        .requestMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/users/refresh-token").permitAll()

                        // Public GET
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/authors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        
                        // Reviews - GET public, POST/PUT/DELETE authenticated
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/*/reviews").permitAll()
                        
                        // Books - POST/PUT/DELETE only ADMIN/LIBRARIAN
                        .requestMatchers(HttpMethod.POST, "/api/v1/books").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/books/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        
                        // Authors - POST/PUT/DELETE only ADMIN/LIBRARIAN
                        .requestMatchers(HttpMethod.POST, "/api/v1/authors").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/authors/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/authors/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        
                        // Categories - POST/PUT/DELETE only ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                        
                        // User endpoints
                        .requestMatchers("/api/v1/users/me").authenticated()
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        
                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
