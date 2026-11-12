package clase.spring.domain.ports;

import clase.spring.domain.models.LoteInfo;

/** Lo que este servicio necesita saber de campos-service. */
public interface CamposPort {

    LoteInfo obtenerLote(Long campoId, Long loteId);
}
