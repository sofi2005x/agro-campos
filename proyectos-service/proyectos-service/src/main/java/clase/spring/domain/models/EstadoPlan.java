package clase.spring.domain.models;

public enum EstadoPlan {
    PLANIFICADO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO;

    /** Un plan activo ocupa el lote: solo PLANIFICADO y EN_CURSO lo son (RN-LAB-02). */
    public boolean esActivo() {
        return this == PLANIFICADO || this == EN_CURSO;
    }
}
