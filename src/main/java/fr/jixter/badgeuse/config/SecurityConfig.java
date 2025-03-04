package fr.jixter.badgeuse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.csrf(CsrfSpec::disable)
        .authorizeExchange(
            exchange ->
                exchange
                    // Autoriser les endpoints Actuator et Swagger UI (à l'URL configurée)
                    .pathMatchers(
                        "/actuator/**",
                        "/swagger-ui/index.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    .anyExchange()
                    .permitAll())
        .build();
  }
}
