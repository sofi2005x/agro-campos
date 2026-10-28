package clase.spring.application.dtos.response;

import clase.spring.domain.models.Campo;
import clase.spring.domain.models.Lote;

public record CampoResponseDTO(
        Long id,
        String nombre,
        Double superficieTotalHas,
        int cantidadLotes,
        Boolean activo
) {
    public static CampoResponseDTO fromEntity(Campo campo) {
        int lotesActivos = campo.getLotes() != null
                ? (int) campo.getLotes().stream().filter(l -> Boolean.TRUE.equals(l.getActivo())).count()
                : 0;

        return new CampoResponseDTO(
                campo.getId(),
                campo.getNombre(),
                campo.getSuperficieTotalHas(),
                lotesActivos,
                campo.getActivo()
        );
    }
}
