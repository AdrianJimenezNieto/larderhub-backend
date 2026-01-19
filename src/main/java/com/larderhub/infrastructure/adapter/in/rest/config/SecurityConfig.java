package com.larderhub.infrastructure.adapter.in.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF para facilitar pruebas iniciales
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/health").permitAll() // Permitimos acceso público al health check
            .anyRequest().authenticated() // El resto requiere autenticación
        );
    return http.build();
  }
}
