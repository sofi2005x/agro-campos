package clase.bda.commons.autoconfigure;

import clase.bda.commons.errors.ApiExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/** Registra el manejo de errores común en los servicios web (servlet). */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonsErrorsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApiExceptionHandler apiExceptionHandler() {
        return new ApiExceptionHandler();
    }
}
