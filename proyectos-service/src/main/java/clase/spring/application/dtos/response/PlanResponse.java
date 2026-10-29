package clase.spring.application.dtos.response;

import clase.spring.domain.models.EstadoPlan;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private Long id;
    private Long campoId;
    private Long loteId;
    private Long cultivoId;
    private String cultivoNombre;
    private EstadoPlan estado;
    private LocalDate fechaSiembraPrevista;
    private LocalDateTime fechaCreacion;
    private List<LaborResponse> labores;

    // Avisos que no impidieron la operación (por ejemplo, clima que no se pudo validar)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> advertencias;
}
