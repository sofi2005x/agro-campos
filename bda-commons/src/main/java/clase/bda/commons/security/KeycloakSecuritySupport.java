package clase.bda.commons.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * La parte de la seguridad que es igual en todos los servicios (resource server con JWT de Keycloak).
 * Lo que NO se comparte a propósito son las reglas de acceso por ruta y rol: cada servicio las define en su propio
 * {@code SecurityFilterChain}, porque dependen de sus endpoints.
 *
 * <pre>{@code
 * @Bean
 * SecurityFilterChain filterChain(HttpSecurity http, SecurityErrorHandler errorHandler) throws Exception {
 *     KeycloakSecuritySupport.configurarResourceServer(http, errorHandler);
 *     http.authorizeHttpRequests(auth -> auth
 *             .requestMatchers(KeycloakSecuritySupport.RUTAS_PUBLICAS).permitAll()
 *             .requestMatchers(HttpMethod.GET, "/api/v1/campos/**").hasAnyRole("ADMIN", "AGRONOMO", "OPERARIO")
 *             .requestMatchers("/api/v1/campos/**").hasRole("ADMIN")
 *             .anyRequest().authenticated());
 *     return http.build();
 * }
 * }</pre>
 */
public final class KeycloakSecuritySupport {

    /** Rutas que no requieren token: health check del compose, Swagger UI y la especificación OpenAPI. */
    public static final String[] RUTAS_PUBLICAS = {
            "/actuator/health/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
    };

    private KeycloakSecuritySupport() {
    }

    /**
     * Sin CSRF ni sesión (la API es stateless), y validación del JWT con los roles de Keycloak. Los errores de
     * autenticación (401) y autorización (403) salen en JSON mediante {@link SecurityErrorHandler}.
     */
    public static HttpSecurity configurarResourceServer(HttpSecurity http, SecurityErrorHandler errorHandler)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth -> oauth
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakRoleConverter())));
    }
}
