package clase.spring.domain.exceptions;

import clase.bda.commons.errors.RecursoNoEncontradoException;

public class PlanNoEncontradoException extends RecursoNoEncontradoException {
    public PlanNoEncontradoException(Long id) {
        super("Plan de siembra no encontrado con ID: " + id);
    }
}
