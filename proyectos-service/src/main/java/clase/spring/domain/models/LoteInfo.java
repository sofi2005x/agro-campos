package clase.spring.domain.models;

/**
 * Datos de un lote de campos-service que necesitan las reglas de este servicio.
 * El tipo de suelo es el código que define agronomia-service (por ejemplo "TIPO_2"): este servicio no tiene
 * su propia copia del enum, de modo que el catálogo de suelos tiene un único dueño.
 */
public record LoteInfo(Long id, Long campoId, Integer nroLote, Double latitud, Double longitud, String tipoSuelo) {

    public boolean tieneCoordenadas() {
        return latitud != null && longitud != null;
    }
}
