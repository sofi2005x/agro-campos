package clase.bda.commons.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce excepciones a respuestas HTTP con el formato {@link ErrorResponse}. Se registra sola en los servicios
 * web que usen bda-commons (ver CommonsErrorsAutoConfiguration). No define un manejador para Exception: así
 * los errores propios de Spring (405, 404 de rutas, etc.) conservan su respuesta estándar.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> handleReglaNegocio(ReglaNegocioException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleServicioNoDisponible(ServicioNoDisponibleException ex) {
        log.error("Servicio remoto no disponible: {}", ex.getMessage(), ex);
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Datos inválidos", errores);
    }

    // Cuerpo ilegible o con un enum inválido (por ejemplo "estado": "XYZ")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleCuerpoIlegible(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido o tiene valores no permitidos", null);
    }

    // Parámetro de URL o query inválido (por ejemplo ?estado=XYZ)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "Valor inválido para el parámetro '" + ex.getName() + "': " + ex.getValue(), null);
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje, Map<String, String> errores) {
        ErrorResponse error = new ErrorResponse(status.value(), status.getReasonPhrase(), mensaje,
                LocalDateTime.now(), errores);
        return ResponseEntity.status(status).body(error);
    }
}
