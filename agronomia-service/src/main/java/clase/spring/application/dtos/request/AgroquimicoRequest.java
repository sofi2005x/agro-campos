package clase.spring.application.dtos.request;

import clase.spring.domain.models.TipoAgroquimico;
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
public class AgroquimicoRequest {
    @NotBlank(message = "El nombre comercial es obligatorio")
    private String nombreComercial;

    @NotBlank(message = "El principio activo es obligatorio")
    private String principioActivo;

    @NotNull(message = "El tipo es obligatorio")
    private TipoAgroquimico tipo;

    @Positive(message = "El viento máximo permitido debe ser positivo")
    private Double vientoMaximoPermitidoKmH;

    @Positive(message = "Las horas libres de lluvia deben ser positivas")
    private Integer horasLibresDeLluviaRequeridas;
}
