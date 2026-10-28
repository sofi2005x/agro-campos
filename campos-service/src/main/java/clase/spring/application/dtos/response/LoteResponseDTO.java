package clase.spring.application.dtos.response;

import clase.spring.domain.models.Lote;
import clase.spring.domain.models.TipoSuelo;

public record LoteResponseDTO(
        Long id,
        Integer nroLote,
        Double hectareas,
        Double latitud,
        Double longitud,
        TipoSuelo tipoSuelo,
        Long campoId,
        Boolean activo
) {
    public static LoteResponseDTO fromEntity(Lote lote) {
        return new LoteResponseDTO(
                lote.getId(),
                lote.getNroLote(),
                lote.getHectareas(),
                lote.getLatitud(),
                lote.getLongitud(),
                lote.getTipoSuelo(),
                lote.getCampo() != null ? lote.getCampo().getId() : null,
                lote.getActivo()
        );
    }
}
