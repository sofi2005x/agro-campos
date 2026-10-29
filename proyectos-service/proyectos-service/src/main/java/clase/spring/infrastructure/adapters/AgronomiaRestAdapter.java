package clase.spring.infrastructure.adapters;

import clase.spring.domain.exceptions.ServicioNoDisponibleException;
import clase.spring.domain.models.CultivoInfo;
import clase.spring.domain.models.TratamientoInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import clase.spring.domain.ports.AgronomiaPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;

/** Adapter de {@link AgronomiaPort}: consume la API REST de agronomia-service (cultivos y tratamientos). */
@Component
public class AgronomiaRestAdapter implements AgronomiaPort {

    private static final String SERVICIO = "agronomia-service";

    private final RestClient restClient;

    public AgronomiaRestAdapter(@Qualifier("agronomiaRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CultivoInfo obtenerCultivo(Long cultivoId) {
        CultivoRemoto cultivo = LlamadaRemota.ejecutar(SERVICIO, "Cultivo no encontrado con ID: " + cultivoId,
                () -> restClient.get()
                        .uri("/api/v1/agronomia/cultivos/{id}", cultivoId)
                        .retrieve()
                        .body(CultivoRemoto.class));
        if (cultivo == null) {
            throw new ServicioNoDisponibleException(SERVICIO, "respuesta vacía");
        }
        return new CultivoInfo(cultivo.id(), cultivo.nombre(), cultivo.suelosAptos(), cultivo.labores());
    }

    @Override
    public TratamientoInfo obtenerTratamiento(Long tratamientoId) {
        TratamientoRemoto tratamiento = LlamadaRemota.ejecutar(SERVICIO, "Tratamiento no encontrado con ID: " + tratamientoId,
                () -> restClient.get()
                        .uri("/api/v1/agronomia/tratamientos/{id}", tratamientoId)
                        .retrieve()
                        .body(TratamientoRemoto.class));
        if (tratamiento == null || tratamiento.cultivo() == null || tratamiento.agroquimico() == null) {
            throw new ServicioNoDisponibleException(SERVICIO, "respuesta incompleta");
        }
        AgroquimicoRemoto agro = tratamiento.agroquimico();
        return new TratamientoInfo(tratamiento.id(), tratamiento.cultivo().id(), agro.nombreComercial(),
                agro.vientoMaximoPermitidoKmH(), agro.horasLibresDeLluviaRequeridas());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CultivoRemoto(Long id, String nombre, Set<String> suelosAptos, List<String> labores) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TratamientoRemoto(Long id, CultivoRef cultivo, AgroquimicoRemoto agroquimico) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CultivoRef(Long id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AgroquimicoRemoto(String nombreComercial, Double vientoMaximoPermitidoKmH,
                                     Integer horasLibresDeLluviaRequeridas) {
    }
}
