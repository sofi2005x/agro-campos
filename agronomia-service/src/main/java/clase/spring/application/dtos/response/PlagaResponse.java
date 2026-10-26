package clase.spring.application.dtos.response;

import clase.spring.domain.models.TipoPlaga;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlagaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private TipoPlaga tipoPlaga;
}
