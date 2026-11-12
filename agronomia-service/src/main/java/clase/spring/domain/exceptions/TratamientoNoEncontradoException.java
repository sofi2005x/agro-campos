package clase.spring.domain.exceptions;

import clase.bda.commons.errors.RecursoNoEncontradoException;

public class TratamientoNoEncontradoException extends RecursoNoEncontradoException {
    public TratamientoNoEncontradoException(Long id) {
        super("Tratamiento no encontrado con ID: " + id);
    }

    public TratamientoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
