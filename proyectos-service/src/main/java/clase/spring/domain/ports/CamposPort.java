package clase.spring.domain.ports;

public interface CamposPort {

    CampoInfo buscarCampo(Long campoId);

    record CampoInfo(
            Long id,
            String nombre,
            Double superficieTotalHas,
            Integer cantidadLotes
    ) {}
}
