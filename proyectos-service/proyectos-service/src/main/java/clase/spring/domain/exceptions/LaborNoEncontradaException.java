package clase.spring.domain.exceptions;

public class LaborNoEncontradaException extends RuntimeException {
    public LaborNoEncontradaException(Long planId, Long laborId) {
        super("Labor no encontrada con ID: " + laborId + " en el plan " + planId);
    }
}
