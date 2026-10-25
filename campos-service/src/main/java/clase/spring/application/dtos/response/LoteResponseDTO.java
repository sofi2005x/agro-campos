package clase.spring.dtos.response;

import clase.spring.models.Lote;
import clase.spring.models.TipoSuelo;

public record LoteResponseDTO(
        Long id,
        Integer nroLote,
        Double hectareas,
        Double latitud,
        Double longitud,
        TipoSuelo tipoSuelo,
        Long campoId
) {
    public static LoteResponseDTO fromEntity(Lote lote) {
        return new LoteResponseDTO(
                lote.getId(),
                lote.getNroLote(),
                lote.getHectareas(),
                lote.getLatitud(),
                lote.getLongitud(),
                lote.getTipoSuelo(),
                lote.getCampo() != null ? lote.getCampo().getId() : null
        );
    }
}