package clase.spring.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CampoRequestDTO(
        @NotBlank(message = "El nombre del campo no puede estar vacío")
        String nombre,

        @NotNull(message = "La superficie total es obligatoria")
        @Positive(message = "La superficie total debe ser mayor a 0")
        Double superficieTotalHas
) {}

