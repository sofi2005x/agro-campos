package clase.spring.application.controllers;

import clase.spring.application.dtos.response.ErrorResponse;
import clase.spring.domain.exceptions.LaborNoEncontradaException;
import clase.spring.domain.exceptions.PlanNoEncontradoException;
import clase.spring.domain.exceptions.ReferenciaNoEncontradaException;
import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.exceptions.ServicioNoDisponibleException;
import lombok.extern.slf4j.Slf4j;
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
 * Traducir las excepciones a HTTP es responsabilidad de la capa de aplicación.
 * Todas las respuestas de error usan el mismo formato (ErrorResponse).
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> handleReglaNegocio(ReglaNegocioException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler({PlanNoEncontradoException.class, LaborNoEncontradaException.class,
            ReferenciaNoEncontradaException.class})
    public ResponseEntity<ErrorResponse> handleNoEncontrado(RuntimeException ex) {
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
        ErrorResponse error = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .mensaje(mensaje)
                .errores(errores)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
