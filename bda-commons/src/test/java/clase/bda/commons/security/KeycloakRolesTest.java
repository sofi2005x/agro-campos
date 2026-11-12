package clase.bda.commons.security;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRolesTest {

    @Test
    void conviertelosRolesDelRealmAAutoridadesDeSpring() {
        Map<String, Object> claims = Map.of("realm_access", Map.of("roles", List.of("ADMIN", "AGRONOMO", "OPERARIO")));

        assertEquals(List.of("ROLE_ADMIN", "ROLE_AGRONOMO", "ROLE_OPERARIO"), KeycloakRoles.autoridadesDesde(claims));
    }

    @Test
    void respetaElNombreDelRolTalCualVieneEnElToken() {
        Map<String, Object> claims = Map.of("realm_access", Map.of("roles", List.of("offline_access")));

        assertEquals(List.of("ROLE_offline_access"), KeycloakRoles.autoridadesDesde(claims));
    }

    @Test
    void sinClaimRealmAccessNoHayAutoridades() {
        assertTrue(KeycloakRoles.autoridadesDesde(Map.of("sub", "u1")).isEmpty());
    }

    @Test
    void sinClaimsNoHayAutoridades() {
        assertTrue(KeycloakRoles.autoridadesDesde(null).isEmpty());
    }

    @Test
    void realmAccessSinRolesNoHayAutoridades() {
        assertTrue(KeycloakRoles.autoridadesDesde(Map.of("realm_access", Map.of())).isEmpty());
    }

    @Test
    void ignoraClaimsConFormatoInesperado() {
        assertTrue(KeycloakRoles.autoridadesDesde(Map.of("realm_access", "no-es-un-mapa")).isEmpty());
        assertTrue(KeycloakRoles.autoridadesDesde(Map.of("realm_access", Map.of("roles", "ADMIN"))).isEmpty());
    }

    @Test
    void ignoraRolesVaciosONulosYLimpiaEspacios() {
        Map<String, Object> realm = new HashMap<>();
        realm.put("roles", java.util.Arrays.asList(" ADMIN ", "", null, "  "));

        assertEquals(List.of("ROLE_ADMIN"), KeycloakRoles.autoridadesDesde(Map.of("realm_access", realm)));
    }
}
