package clase.bda.commons.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

/**
 * Convierte un JWT de Keycloak en la autenticación de Spring Security, con los roles del realm como autoridades
 * ("ROLE_ADMIN", "ROLE_AGRONOMO"...). Se usa en la configuración de cada servicio:
 * {@code .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter)))}
 */
public class KeycloakRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<GrantedAuthority> autoridades = KeycloakRoles.autoridadesDesde(jwt.getClaims()).stream()
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();
        return new JwtAuthenticationToken(jwt, autoridades);
    }
}
