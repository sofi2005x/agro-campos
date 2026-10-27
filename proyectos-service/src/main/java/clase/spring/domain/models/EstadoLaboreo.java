package clase.spring.domain.models;

public enum EstadoLaboreo {
    PENDIENTE,
    EN_EJECUCION,
    COMPLETADA;

    public boolean puedeCambiarA(EstadoLaboreo nuevo) {
        switch (this) {
            case PENDIENTE:
                return nuevo == EN_EJECUCION;
            case EN_EJECUCION:
                return nuevo == COMPLETADA;
            case COMPLETADA:
                return false;
            default:
                return false;
        }
    }
}
