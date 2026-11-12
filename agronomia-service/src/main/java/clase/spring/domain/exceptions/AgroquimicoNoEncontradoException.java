package clase.spring.domain.exceptions;

import clase.bda.commons.errors.RecursoNoEncontradoException;

public class AgroquimicoNoEncontradoException extends RecursoNoEncontradoException {
    public AgroquimicoNoEncontradoException(Long id) {
        super("Agroquímico no encontrado con ID: " + id);
    }

    public AgroquimicoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
