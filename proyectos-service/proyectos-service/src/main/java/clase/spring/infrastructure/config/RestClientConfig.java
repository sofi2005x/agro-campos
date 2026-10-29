package clase.spring.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Un RestClient por servicio consumido, cada uno con su URL base y sus timeouts.
 * Los timeouts cortos evitan que un servicio lento deje colgado a este servicio.
 */
@Configuration
public class RestClientConfig {

    @Bean
    RestClient camposRestClient(@Value("${app.campos.base-url}") String baseUrl,
                                @Value("${app.http.timeout-ms:3000}") int timeoutMs) {
        return construir(baseUrl, timeoutMs);
    }

    @Bean
    RestClient agronomiaRestClient(@Value("${app.agronomia.base-url}") String baseUrl,
                                   @Value("${app.http.timeout-ms:3000}") int timeoutMs) {
        return construir(baseUrl, timeoutMs);
    }

    @Bean
    RestClient climaRestClient(@Value("${clima.open-meteo.base-url}") String baseUrl,
                               @Value("${clima.open-meteo.timeout-ms:3000}") int timeoutMs) {
        return construir(baseUrl, timeoutMs);
    }

    private RestClient construir(String baseUrl, int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
