package clase.spring.application.dtos.response;

import clase.spring.domain.models.EstadoLabor;
import clase.spring.domain.models.TipoLabor;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LaborResponse {
    private Long id;
    private Integer orden;
    private String nombre;
    private TipoLabor tipo;
    private EstadoLabor estado;
    private Long tratamientoId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}
