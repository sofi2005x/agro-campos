package clase.spring.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI proyectosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Proyectos")
                        .description("Microservicio de planes de siembra: estados, laboreos, exclusividad del lote "
                                + "y control climático de fumigaciones")
                        .version("v1.0.0"));
    }
}
