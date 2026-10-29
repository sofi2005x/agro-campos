package clase.spring.domain.ports;

import clase.spring.domain.models.CultivoInfo;
import clase.spring.domain.models.TratamientoInfo;

/** Lo que este servicio necesita saber del catálogo agronómico de agronomia-service. */
public interface AgronomiaPort {

    /**
     * @throws clase.spring.domain.exceptions.ReferenciaNoEncontradaException si el cultivo no existe
     * @throws clase.spring.domain.exceptions.ServicioNoDisponibleException   si agronomia-service no responde
     */
    CultivoInfo obtenerCultivo(Long cultivoId);

    /**
     * @throws clase.spring.domain.exceptions.ReferenciaNoEncontradaException si el tratamiento no existe
     * @throws clase.spring.domain.exceptions.ServicioNoDisponibleException   si agronomia-service no responde
     */
    TratamientoInfo obtenerTratamiento(Long tratamientoId);
}
