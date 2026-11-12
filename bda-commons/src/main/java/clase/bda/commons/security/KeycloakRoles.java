package clase.bda.commons.security;

import java.util.List;
import java.util.Map;

/**
 * Lógica pura (sin Spring) para leer los roles de un token de Keycloak. Keycloak los envía en el claim
 * {@code realm_access}: {@code {"roles": ["ADMIN", "USUARIO"]}}. Spring Security espera el prefijo "ROLE_" delante del nombre; el nombre se respeta tal cual (los roles del realm bda-realm ya son ADMIN, AGRONOMO y OPERARIO).
 * A diferencia del ejemplo del apunte 21, tolera tokens sin ese claim (devuelve lista vacía en vez de fallar).
 */
public final class KeycloakRoles {

    public static final String CLAIM_REALM_ACCESS = "realm_access";
    public static final String PREFIJO_ROL = "ROLE_";

    private KeycloakRoles() {
    }

    public static List<String> autoridadesDesde(Map<String, Object> claims) {
        if (claims == null || !(claims.get(CLAIM_REALM_ACCESS) instanceof Map<?, ?> realmAccess)) {
            return List.of();
        }
        if (!(realmAccess.get("roles") instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .filter(rol -> rol != null && !rol.toString().isBlank())
                .map(rol -> PREFIJO_ROL + rol.toString().trim())
                .toList();
    }
}
