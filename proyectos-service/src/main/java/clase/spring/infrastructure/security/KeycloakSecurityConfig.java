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
                        // Iniciar y completar labores: los tres roles (va ANTES que la regla general)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/proyectos/planes/*/labores/*")
                        .hasAnyRole("ADMIN", "AGRONOMO", "OPERARIO")
                        // Consultas: los tres roles
                        .requestMatchers(HttpMethod.GET, "/api/v1/proyectos/**")
                        .hasAnyRole("ADMIN", "AGRONOMO", "OPERARIO")
                        // Crear y cancelar planes (POST y PATCH de plan): ADMIN y AGRONOMO
                        .requestMatchers("/api/v1/proyectos/**")
                        .hasAnyRole("ADMIN", "AGRONOMO")
                        .anyRequest().authenticated());

        return http.build();
    }
}