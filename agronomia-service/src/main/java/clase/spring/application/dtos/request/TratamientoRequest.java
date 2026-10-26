package clase.spring.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TratamientoRequest {
    @NotNull(message = "El id de la plaga es obligatorio")
    private Long plagaId;

    @NotNull(message = "El id del agroquímico es obligatorio")
    private Long agroquimicoId;

    @NotNull(message = "El id del cultivo es obligatorio")
    private Long cultivoId;

    @NotNull(message = "La dosis es obligatoria")
    @Positive(message = "La dosis debe ser mayor a 0")
    private Double dosisPorHectarea;

    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidadMedida;
}
