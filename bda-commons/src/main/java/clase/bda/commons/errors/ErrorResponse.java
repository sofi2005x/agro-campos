package clase.bda.commons.errors;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato único de error para todas las APIs del sistema.
 * {@code errores} solo se informa en errores de validación (campo → mensaje).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(int status, String error, String mensaje, LocalDateTime timestamp,
                            Map<String, String> errores) {
}
