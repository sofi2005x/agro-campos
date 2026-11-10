package clase.spring.domain.models;

public enum TipoPlaga {
    INSECTO(TipoAgroquimico.INSECTICIDA),
    HONGO(TipoAgroquimico.FUNGICIDA),
    MALEZA(TipoAgroquimico.HERBICIDA);

    private final TipoAgroquimico tipoQueCombate;

    TipoPlaga(TipoAgroquimico tipoQueCombate) {
        this.tipoQueCombate = tipoQueCombate;
    }

    /**
     * RN-TRA-04: coherencia entre el tipo de plaga y el tipo de agroquímico.
     * Un FERTILIZANTE no combate ninguna plaga, por lo que nunca es admitido.
     */
    public boolean admite(TipoAgroquimico tipoAgroquimico) {
        return tipoAgroquimico == tipoQueCombate;
    }

    public TipoAgroquimico getTipoQueCombate() {
        return tipoQueCombate;
    }
}
