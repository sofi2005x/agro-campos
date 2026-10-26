package clase.spring.application.dtos.response;

import clase.spring.domain.models.TipoAgroquimico;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgroquimicoResponse {
    private Long id;
    private String nombreComercial;
    private String principioActivo;
    private TipoAgroquimico tipo;
    private Double vientoMaximoPermitidoKmH;
    private Integer horasLibresDeLluviaRequeridas;
}
