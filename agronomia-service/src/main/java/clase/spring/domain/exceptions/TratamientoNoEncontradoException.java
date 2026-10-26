package clase.spring.domain.exceptions;

public class TratamientoNoEncontradoException extends RuntimeException {
    public TratamientoNoEncontradoException(Long id) {
        super("Tratamiento no encontrado con ID: " + id);
    }

    public TratamientoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
