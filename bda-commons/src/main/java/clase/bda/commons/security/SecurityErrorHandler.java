package clase.bda.commons.security;

import clase.bda.commons.errors.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Responde en JSON, con el mismo formato {@link ErrorResponse} que el resto de la API, cuando falla la seguridad:
 * 401 si falta el token o no es válido, 403 si el rol no alcanza. Sin esto Spring Security responde con una
 * página o un cuerpo vacío, porque estos errores ocurren antes de llegar a cualquier controller.
 */
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        escribir(response, HttpStatus.UNAUTHORIZED, "Autenticación requerida: falta el token o no es válido");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escribir(response, HttpStatus.FORBIDDEN, "No tenés permisos para realizar esta operación");
    }

    private void escribir(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), mensaje,
                LocalDateTime.now(), null);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
