package clase.spring.domain.exceptions;

import clase.spring.application.dtos.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> handleReglaNegocio(ReglaNegocioException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({CultivoNoEncontradoException.class, PlagaNoEncontradaException.class,
            AgroquimicoNoEncontradoException.class, TratamientoNoEncontradoException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .mensaje("Datos inválidos")
                .errores(errores)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);
    }
}
