package clase.spring.infrastructure.exceptions;

public class CampoNoEncontradoException extends RuntimeException {

    public CampoNoEncontradoException(Long campoId) {
        super("No existe el campo con ID: " + campoId);
    }
}
