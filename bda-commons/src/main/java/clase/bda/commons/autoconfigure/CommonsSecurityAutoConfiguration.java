package clase.bda.commons.autoconfigure;

import clase.bda.commons.security.SecurityErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Registra {@link SecurityErrorHandler} como bean, pero solo si el servicio ya incluye Spring Security con
 * OAuth2 Resource Server. Si no, esta clase se ignora y bda-commons no activa la seguridad por su cuenta.
 */
@AutoConfiguration(after = JacksonAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({JwtAuthenticationToken.class, AuthenticationEntryPoint.class})
public class CommonsSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecurityErrorHandler securityErrorHandler(ObjectMapper objectMapper) {
        return new SecurityErrorHandler(objectMapper);
    }
}
