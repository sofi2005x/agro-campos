package clase.spring.application.dtos.request;

import clase.spring.domain.models.EstadoLabor;
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
public class LaborPatchRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoLabor estado;

    // Obligatorio al iniciar una fumigación: de este tratamiento salen los límites de viento y lluvia
    @Positive(message = "El ID del tratamiento debe ser positivo")
    private Long tratamientoId;
}
