package clase.spring.infrastructure.security;

import clase.bda.commons.security.KeycloakRoleConverter;
import clase.bda.commons.security.KeycloakSecuritySupport;
import clase.bda.commons.security.SecurityErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class KeycloakSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityErrorHandler errorHandler) throws Exception {
        KeycloakSecuritySupport.configurarResourceServer(http, errorHandler);
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(KeycloakSecuritySupport.RUTAS_PUBLICAS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/agronomia/**")
                        .hasAnyRole("ADMIN", "AGRONOMO", "OPERARIO")
                        .requestMatchers("/api/v1/agronomia/**")
                        .hasAnyRole("ADMIN", "AGRONOMO")
                        .anyRequest().authenticated());

        return http.build();
    }

}