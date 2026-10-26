package clase.spring.application.dtos.request;

import clase.spring.domain.models.TipoPlaga;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlagaRequest {
    @NotBlank(message = "El nombre de la plaga es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El tipo de plaga es obligatorio")
    private TipoPlaga tipoPlaga;
}
