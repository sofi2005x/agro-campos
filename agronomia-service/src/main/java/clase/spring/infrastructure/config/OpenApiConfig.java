package clase.spring.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agronomiaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Agronomía")
                        .description("Microservicio de catálogo agronómico: cultivos, plagas, agroquímicos y tratamientos")
                        .version("v1.0.0"));
    }
}
