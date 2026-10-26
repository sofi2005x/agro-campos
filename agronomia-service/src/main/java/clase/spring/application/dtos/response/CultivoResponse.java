package clase.spring.application.dtos.response;

import clase.spring.domain.models.TipoSuelo;
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
public class CultivoResponse {
    private Long id;
    private String nombre;
    private Set<TipoSuelo> suelosAptos;
    private List<String> labores;
}
