package clase.spring.infrastructure.adapters;

import clase.spring.domain.exceptions.ReferenciaNoEncontradaException;
import clase.spring.domain.exceptions.ServicioNoDisponibleException;
import clase.spring.domain.models.LoteInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import clase.spring.domain.ports.CamposPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;

/**
 * Adapter de {@link CamposPort}: consume la API REST de campos-service. Hoy ese servicio solo expone los lotes de un campo
 * (GET /api/v1/campos/{campoId}/lotes), por eso se busca el lote dentro de esa lista.
 */
@Component
public class CamposRestAdapter implements CamposPort {

    private static final String SERVICIO = "campos-service";

    private final RestClient restClient;

    public CamposRestAdapter(@Qualifier("camposRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public LoteInfo obtenerLote(Long campoId, Long loteId) {
        LoteRemoto[] lotes = LlamadaRemota.ejecutar(SERVICIO, "Campo no encontrado con ID: " + campoId,
                () -> restClient.get()
                        .uri("/api/v1/campos/{campoId}/lotes", campoId)
                        .retrieve()
                        .body(LoteRemoto[].class));

        if (lotes == null) {
            throw new ServicioNoDisponibleException(SERVICIO, "respuesta vacía");
        }
        return Arrays.stream(lotes)
                .filter(lote -> loteId.equals(lote.id()))
                .findFirst()
                .map(lote -> new LoteInfo(lote.id(), campoId, lote.nroLote(), lote.latitud(), lote.longitud(), lote.tipoSuelo()))
                .orElseThrow(() -> new ReferenciaNoEncontradaException(
                        "Lote no encontrado con ID: " + loteId + " en el campo " + campoId));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LoteRemoto(Long id, Integer nroLote, Double latitud, Double longitud, String tipoSuelo) {
    }
}
