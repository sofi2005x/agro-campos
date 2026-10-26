package clase.spring.domain.exceptions;

public class PlagaNoEncontradaException extends RuntimeException {
    public PlagaNoEncontradaException(Long id) {
        super("Plaga no encontrada con ID: " + id);
    }

    public PlagaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
