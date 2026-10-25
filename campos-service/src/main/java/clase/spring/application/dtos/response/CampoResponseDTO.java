package clase.spring.dtos.response;

import clase.spring.models.Campo;

public record CampoResponseDTO(
        Long id,
        String nombre,
        Double superficieTotalHas,
        int cantidadLotes
) {
    public static CampoResponseDTO fromEntity(Campo campo) {
        return new CampoResponseDTO(
                campo.getId(),
                campo.getNombre(),
                campo.getSuperficieTotalHas(),
                campo.getLotes() != null ? campo.getLotes().size() : 0
        );
    }
}