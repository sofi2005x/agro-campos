package clase.spring.application.dtos.request;

import clase.spring.domain.models.TipoSuelo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CultivoRequest {
    @NotBlank(message = "El nombre del cultivo es obligatorio")
    private String nombre;

    @NotEmpty(message = "Debe indicar al menos un suelo apto")
    private Set<TipoSuelo> suelosAptos;

    private List<String> labores;
}
