package clase.spring.application.dtos.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequest {

    @NotNull(message = "El campo es obligatorio")
    @Positive(message = "El ID del campo debe ser positivo")
    private Long campoId;

    @NotNull(message = "El lote es obligatorio")
    @Positive(message = "El ID del lote debe ser positivo")
    private Long loteId;

    @NotNull(message = "El cultivo es obligatorio")
    @Positive(message = "El ID del cultivo debe ser positivo")
    private Long cultivoId;

    @NotNull(message = "La fecha de siembra prevista es obligatoria")
    @FutureOrPresent(message = "La fecha de siembra prevista no puede ser pasada")
    private LocalDate fechaSiembraPrevista;
}
