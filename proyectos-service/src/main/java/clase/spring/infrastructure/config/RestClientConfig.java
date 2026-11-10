package clase.spring.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Un RestClient por servicio consumido, cada uno con su URL base, sus timeouts y si puede reenviar un token.
 * Los timeouts cortos evitan que un servicio lento deje colgado a este servicio.
 */
@Configuration
public class RestClientConfig {

    @Bean
    RestClient camposRestClient(@Value("${app.campos.base-url}") String baseUrl,
                                @Value("${app.http.timeout-ms:3000}") int timeoutMs) {
        return construir(baseUrl, timeoutMs, true);
    }

    @Bean
    RestClient agronomiaRestClient(@Value("${app.agronomia.base-url}") String baseUrl,
                                   @Value("${app.http.timeout-ms:3000}") int timeoutMs) {
        return construir(baseUrl, timeoutMs, true);
    }

    @Bean
    RestClient climaRestClient(@Value("${clima.open-meteo.base-url}") String baseUrl,
                               @Value("${clima.open-meteo.timeout-ms:3000}") int timeoutMs) {
        return construir(baseUrl, timeoutMs, false);
    }

    private RestClient construir(String baseUrl, int timeoutMs, boolean reenviarToken) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory);

        if (reenviarToken) {
            builder.requestInterceptor(propagarToken());
        }
        return builder.build();
       // return RestClient.builder()
        //        .baseUrl(baseUrl)
          //      .requestFactory(factory)
            //    .build();
    }

    private ClientHttpRequestInterceptor propagarToken() {
        return (request, body, execution) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                request.getHeaders().setBearerAuth(jwtAuth.getToken().getTokenValue());
            }
            return execution.execute(request, body);
        };
    }
}
