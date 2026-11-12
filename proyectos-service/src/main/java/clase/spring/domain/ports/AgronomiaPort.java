package clase.spring.domain.ports;

import clase.spring.domain.models.CultivoInfo;
import clase.spring.domain.models.TratamientoInfo;

/** Lo que este servicio necesita saber del catálogo agronómico de agronomia-service. */
public interface AgronomiaPort {

    CultivoInfo obtenerCultivo(Long cultivoId);

    TratamientoInfo obtenerTratamiento(Long tratamientoId);
}
