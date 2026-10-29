package clase.spring.domain.models;

import java.util.List;
import java.util.Set;

/** Datos de un cultivo de agronomia-service: suelos donde puede sembrarse y su secuencia de labores. */
public record CultivoInfo(Long id, String nombre, Set<String> suelosAptos, List<String> labores) {

    public CultivoInfo {
        suelosAptos = suelosAptos == null ? Set.of() : Set.copyOf(suelosAptos);
        labores = labores == null ? List.of() : List.copyOf(labores);
    }

    public boolean esAptoParaSuelo(String suelo) {
        return suelo != null && suelosAptos.contains(suelo);
    }
}
