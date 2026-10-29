package clase.spring.domain.models;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Clasificación de una labor. agronomia-service define las labores de un cultivo como texto libre
 * (por ejemplo "Arar", "Fumigar", "Cosechar"), por eso el tipo se deduce del nombre, ignorando
 * mayúsculas y tildes. Solo interesan los tipos con reglas propias.
 */
public enum TipoLabor {
    ARAR,
    RASTRILLAR,
    SEMBRAR,
    ESCARDILLAR,
    COSECHAR,
    ROLAR,
    FUMIGAR;


    /** BORRAR DESPUÉS
    * Este método reconoce los tipos de labores de cada cultivo, por lo que es necesario normalizarlos y
    * luego compararlos con el enum
    */
    public static TipoLabor desde(String nombre) {
        if (nombre == null) {
            return null;
        }

        String normalizado = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        for (TipoLabor tipo : values()) {
            String valor = tipo.name().toLowerCase(Locale.ROOT);
            String raiz = valor.substring(0, Math.min(5, valor.length()));
            if (normalizado.contains(valor) || normalizado.contains(raiz)) {
                return tipo;
            }
        }

        return null;
    }
}
