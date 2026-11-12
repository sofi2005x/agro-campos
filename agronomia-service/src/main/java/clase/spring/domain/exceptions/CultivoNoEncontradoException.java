package clase.spring.domain.exceptions;

import clase.bda.commons.errors.RecursoNoEncontradoException;

public class CultivoNoEncontradoException extends RecursoNoEncontradoException {
    public CultivoNoEncontradoException(Long id) {
        super("Cultivo no encontrado con ID: " + id);
    }

    public CultivoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
