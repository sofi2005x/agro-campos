package clase.spring.infrastructure.clients;

public record CampoResponse(
        Long id,
        String nombre,
        Double superficieTotalHas,
        Integer cantidadLotes
) { }
