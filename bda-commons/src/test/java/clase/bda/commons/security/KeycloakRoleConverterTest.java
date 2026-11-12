package clase.bda.commons.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    private Jwt jwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("usuario1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60));
        claims.forEach(builder::claim);
        return builder.build();
    }

    @Test
    void laAutenticacionTieneLosRolesDelToken() {
        var autenticacion = converter.convert(jwt(Map.of("realm_access", Map.of("roles", List.of("ADMIN", "USUARIO")))));

        List<String> autoridades = autenticacion.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USUARIO"), autoridades);
        assertEquals("usuario1", autenticacion.getName());
    }

    @Test
    void unTokenSinRolesQuedaSinAutoridades() {
        var autenticacion = converter.convert(jwt(Map.of("scope", "openid")));

        assertTrue(autenticacion.getAuthorities().isEmpty());
    }
}
