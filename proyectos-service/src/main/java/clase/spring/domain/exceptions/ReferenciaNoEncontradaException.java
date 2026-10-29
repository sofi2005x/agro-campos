package clase.spring.domain.exceptions;

/**
 * Un recurso que vive en otro microservicio (campo, lote, cultivo, tratamiento) no existe.
 */
public class ReferenciaNoEncontradaException extends RuntimeException {
    public ReferenciaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
