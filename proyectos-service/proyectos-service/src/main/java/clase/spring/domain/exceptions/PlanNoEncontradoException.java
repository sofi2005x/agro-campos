package clase.spring.domain.exceptions;

public class PlanNoEncontradoException extends RuntimeException {
    public PlanNoEncontradoException(Long id) {
        super("Plan de siembra no encontrado con ID: " + id);
    }
}
