package clase.spring.domain.models;

public enum EstadoLaboreo {
    PENDIENTE,
    EN_EJECUCION,
    COMPLETADA;

    public boolean puedeCambiarA(EstadoLaboreo nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == EN_EJECUCION;
            case EN_EJECUCION -> nuevo == COMPLETADA;
            case COMPLETADA -> false;
        };
    }
}
