package clase.spring.domain.exceptions;

public class AgroquimicoNoEncontradoException extends RuntimeException {
    public AgroquimicoNoEncontradoException(Long id) {
        super("Agroquímico no encontrado con ID: " + id);
    }

    public AgroquimicoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
