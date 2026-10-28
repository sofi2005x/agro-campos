package clase.spring.application.dtos.request;

import clase.spring.domain.models.TipoSuelo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoteRequestDTO(
        @NotNull(message = "El número de lote es obligatorio")
        Integer nroLote,

        @NotNull(message = "Las hectáreas son obligatorias")
        @Positive(message = "Las hectáreas deben ser mayores a 0")
        Double hectareas,

        Double latitud,
        Double longitud,

        @NotNull(message = "El tipo de suelo es obligatorio")
        TipoSuelo tipoSuelo
) {}
