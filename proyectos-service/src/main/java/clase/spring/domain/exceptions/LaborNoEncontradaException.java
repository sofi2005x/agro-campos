package clase.spring.domain.exceptions;

import clase.bda.commons.errors.RecursoNoEncontradoException;

public class LaborNoEncontradaException extends RecursoNoEncontradoException {
    public LaborNoEncontradaException(Long planId, Long laborId) {
        super("Labor no encontrada con ID: " + laborId + " en el plan " + planId);
    }
}
