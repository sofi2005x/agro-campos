package clase.spring.domain.exceptions;

public class CultivoNoEncontradoException extends RuntimeException {
    public CultivoNoEncontradoException(Long id) {
        super("Cultivo no encontrado con ID: " + id);
    }

    public CultivoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
