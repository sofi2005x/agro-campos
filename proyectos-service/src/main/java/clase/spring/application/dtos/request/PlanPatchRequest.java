package clase.spring.application.dtos.request;

import clase.spring.domain.models.EstadoPlan;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanPatchRequest {

    // Hoy la única transición que se pide por API es CANCELADO; el resto se deriva de las labores
    @NotNull(message = "El estado es obligatorio")
    private EstadoPlan estado;
}
