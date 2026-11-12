package clase.spring.domain.exceptions;

import clase.bda.commons.errors.RecursoNoEncontradoException;

public class PlagaNoEncontradaException extends RecursoNoEncontradoException {
    public PlagaNoEncontradaException(Long id) {
        super("Plaga no encontrada con ID: " + id);
    }

    public PlagaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
