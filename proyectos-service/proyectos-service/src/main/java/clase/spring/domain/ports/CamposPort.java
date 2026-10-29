package clase.spring.domain.ports;

import clase.spring.domain.models.LoteInfo;

/** Lo que este servicio necesita saber de campos-service. */
public interface CamposPort {

    /**
     * @throws clase.spring.domain.exceptions.ReferenciaNoEncontradaException si el campo o el lote no existen
     * @throws clase.spring.domain.exceptions.ServicioNoDisponibleException   si campos-service no responde
     */
    LoteInfo obtenerLote(Long campoId, Long loteId);
}
