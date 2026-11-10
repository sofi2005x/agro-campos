package clase.spring.application.services;

import clase.spring.application.dtos.request.TratamientoRequest;
import clase.spring.application.dtos.response.AgroquimicoResponse;
import clase.spring.application.dtos.response.CultivoResponse;
import clase.spring.application.dtos.response.PlagaResponse;
import clase.spring.application.dtos.response.TratamientoResponse;
import clase.spring.domain.exceptions.AgroquimicoNoEncontradoException;
import clase.spring.domain.exceptions.CultivoNoEncontradoException;
import clase.spring.domain.exceptions.PlagaNoEncontradaException;
import clase.spring.domain.exceptions.TratamientoNoEncontradoException;
import clase.spring.domain.models.Agroquimico;
import clase.spring.domain.models.Cultivo;
import clase.spring.domain.models.Plaga;
import clase.spring.domain.models.Tratamiento;
import clase.spring.infrastructure.repositories.AgroquimicoRepository;
import clase.spring.infrastructure.repositories.CultivoRepository;
import clase.spring.infrastructure.repositories.PlagaRepository;
import clase.spring.infrastructure.repositories.TratamientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TratamientoService {

    private final TratamientoRepository tratamientoRepository;
    private final PlagaRepository plagaRepository;
    private final AgroquimicoRepository agroquimicoRepository;
    private final CultivoRepository cultivoRepository;

    public List<TratamientoResponse> listar(Long plagaId, Long cultivoId) {
        List<Tratamiento> tratamientos;
        if (plagaId != null && cultivoId != null) {
            tratamientos = tratamientoRepository.findByActivoTrue().stream()
                    .filter(t -> t.getPlaga().getId().equals(plagaId) && t.getCultivo().getId().equals(cultivoId))
                    .collect(Collectors.toList());
        } else if (plagaId != null) {
            tratamientos = tratamientoRepository.findByPlagaIdAndActivoTrue(plagaId);
        } else if (cultivoId != null) {
            tratamientos = tratamientoRepository.findByCultivoIdAndActivoTrue(cultivoId);
        } else {
            tratamientos = tratamientoRepository.findByActivoTrue();
        }
        return tratamientos.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TratamientoResponse obtenerPorId(Long id) {
        Tratamiento tratamiento = tratamientoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new TratamientoNoEncontradoException(id));
        return toResponse(tratamiento);
    }

    public TratamientoResponse crear(TratamientoRequest request) {
        Tratamiento tratamiento = Tratamiento.crear(
                obtenerPlaga(request.getPlagaId()),
                obtenerAgroquimico(request.getAgroquimicoId()),
                obtenerCultivo(request.getCultivoId()),
                request.getDosisPorHectarea(),
                request.getUnidadMedida());
        Tratamiento guardado = tratamientoRepository.save(tratamiento);
        return toResponse(guardado);
    }

    public TratamientoResponse actualizar(Long id, TratamientoRequest request) {
        Tratamiento tratamiento = tratamientoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new TratamientoNoEncontradoException(id));
        tratamiento.actualizar(
                obtenerPlaga(request.getPlagaId()),
                obtenerAgroquimico(request.getAgroquimicoId()),
                obtenerCultivo(request.getCultivoId()),
                request.getDosisPorHectarea(),
                request.getUnidadMedida());
        return toResponse(tratamientoRepository.save(tratamiento));
    }

    public void eliminar(Long id) {
        Tratamiento tratamiento = tratamientoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new TratamientoNoEncontradoException(id));
        tratamiento.setActivo(false);
        tratamientoRepository.save(tratamiento);
    }

    private Plaga obtenerPlaga(Long plagaId) {
        return plagaRepository.findByIdAndActivoTrue(plagaId)
                .orElseThrow(() -> new PlagaNoEncontradaException(plagaId));
    }

    private Agroquimico obtenerAgroquimico(Long agroquimicoId) {
        return agroquimicoRepository.findByIdAndActivoTrue(agroquimicoId)
                .orElseThrow(() -> new AgroquimicoNoEncontradoException(agroquimicoId));
    }

    private Cultivo obtenerCultivo(Long cultivoId) {
        return cultivoRepository.findByIdAndActivoTrue(cultivoId)
                .orElseThrow(() -> new CultivoNoEncontradoException(cultivoId));
    }

    private TratamientoResponse toResponse(Tratamiento tratamiento) {
        PlagaResponse plagaResponse = PlagaResponse.builder()
                .id(tratamiento.getPlaga().getId())
                .nombre(tratamiento.getPlaga().getNombre())
                .descripcion(tratamiento.getPlaga().getDescripcion())
                .tipoPlaga(tratamiento.getPlaga().getTipoPlaga())
                .build();

        AgroquimicoResponse agroquimicoResponse = AgroquimicoResponse.builder()
                .id(tratamiento.getAgroquimico().getId())
                .nombreComercial(tratamiento.getAgroquimico().getNombreComercial())
                .principioActivo(tratamiento.getAgroquimico().getPrincipioActivo())
                .tipo(tratamiento.getAgroquimico().getTipo())
                .vientoMaximoPermitidoKmH(tratamiento.getAgroquimico().getVientoMaximoPermitidoKmH())
                .horasLibresDeLluviaRequeridas(tratamiento.getAgroquimico().getHorasLibresDeLluviaRequeridas())
                .build();

        CultivoResponse cultivoResponse = CultivoResponse.builder()
                .id(tratamiento.getCultivo().getId())
                .nombre(tratamiento.getCultivo().getNombre())
                .suelosAptos(tratamiento.getCultivo().getSuelosAptos())
                .labores(tratamiento.getCultivo().getLabores())
                .build();

        return TratamientoResponse.builder()
                .id(tratamiento.getId())
                .plaga(plagaResponse)
                .agroquimico(agroquimicoResponse)
                .cultivo(cultivoResponse)
                .dosisPorHectarea(tratamiento.getDosisPorHectarea())
                .unidadMedida(tratamiento.getUnidadMedida())
                .build();
    }
}
