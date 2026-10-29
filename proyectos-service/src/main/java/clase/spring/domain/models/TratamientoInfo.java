package clase.spring.domain.models;

/**
 * Datos de un tratamiento de agronomia-service y los límites climáticos de su agroquímico.
 * Si el catálogo no informa un límite se usan los mismos valores por defecto que el agroquímico.
 */
public record TratamientoInfo(Long id, Long cultivoId, String agroquimicoNombre,
                              Double vientoMaximoKmH, Integer horasLibresLluvia) {

    public static final double VIENTO_MAXIMO_POR_DEFECTO_KMH = 15.0;
    public static final int HORAS_LIBRES_LLUVIA_POR_DEFECTO = 4;

    public TratamientoInfo {
        vientoMaximoKmH = vientoMaximoKmH == null ? VIENTO_MAXIMO_POR_DEFECTO_KMH : vientoMaximoKmH;
        horasLibresLluvia = horasLibresLluvia == null ? HORAS_LIBRES_LLUVIA_POR_DEFECTO : horasLibresLluvia;
    }
}
