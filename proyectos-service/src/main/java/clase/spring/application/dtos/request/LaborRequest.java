package clase.spring.application.dtos.request;


import clase.spring.domain.models.EstadoLaboreo;
import clase.spring.domain.models.TipoLaboreo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LaborRequest(

    @NotNull(message = "El tipo de laboreo es obligatorio")
    TipoLaboreo nombre,

    @NotBlank(message = "Debe existir una descripcion para el laboreo")
    String descripcion,

    @NotNull(message = "El estado debe ser obligatorio")
    EstadoLaboreo estadoLaboreo

) {}