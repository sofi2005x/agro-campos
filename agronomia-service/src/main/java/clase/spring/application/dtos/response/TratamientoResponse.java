package clase.spring.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TratamientoResponse {
    private Long id;
    private PlagaResponse plaga;
    private AgroquimicoResponse agroquimico;
    private CultivoResponse cultivo;
    private Double dosisPorHectarea;
    private String unidadMedida;
}
