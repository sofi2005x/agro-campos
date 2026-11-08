package clase.spring.infrastructure.security;

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
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           SecurityErrorHandler errorHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/swagger-ui/**",
                                "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/agronomia/**")
                        .hasAnyRole("ADMIN", "AGRONOMO", "OPERARIO")
                        .requestMatchers("/api/v1/agronomia/**")
                        .hasAnyRole("ADMIN", "AGRONOMO")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakRoleConverter())));

        return http.build();
    }
}