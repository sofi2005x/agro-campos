package clase.spring.application.services;

import clase.spring.application.dtos.request.TratamientoRequest;
import clase.spring.application.dtos.response.AgroquimicoResponse;
import clase.spring.application.dtos.response.CultivoResponse;
import clase.spring.application.dtos.response.PlagaResponse;
import clase.spring.application.dtos.response.TratamientoResponse;
import clase.spring.domain.exceptions.AgroquimicoNoEncontradoException;
import clase.spring.domain.exceptions.CultivoNoEncontradoException;
import clase.spring.domain.exceptions.PlagaNoEncontradaException;
import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.exceptions.TratamientoNoEncontradoException;
import clase.spring.domain.models.Agroquimico;
import clase.spring.domain.models.Cultivo;
import clase.spring.domain.models.Plaga;
import clase.spring.domain.models.TipoAgroquimico;
import clase.spring.domain.models.TipoPlaga;
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
        Tratamiento tratamiento = Tratamiento.builder()
                .activo(true)
                .build();
        aplicarDatos(tratamiento, request);
        Tratamiento guardado = tratamientoRepository.save(tratamiento);
        return toResponse(guardado);
    }

    public TratamientoResponse actualizar(Long id, TratamientoRequest request) {
        Tratamiento tratamiento = tratamientoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new TratamientoNoEncontradoException(id));
        aplicarDatos(tratamiento, request);
        return toResponse(tratamientoRepository.save(tratamiento));
    }

    public void eliminar(Long id) {
        Tratamiento tratamiento = tratamientoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new TratamientoNoEncontradoException(id));
        tratamiento.setActivo(false);
        tratamientoRepository.save(tratamiento);
    }

    private void aplicarDatos(Tratamiento tratamiento, TratamientoRequest request) {
        if (request.getDosisPorHectarea() == null || request.getDosisPorHectarea() <= 0) {
            throw new ReglaNegocioException("La dosis por hectárea debe ser estrictamente positiva (> 0)");
        }

        Plaga plaga = plagaRepository.findByIdAndActivoTrue(request.getPlagaId())
                .orElseThrow(() -> new PlagaNoEncontradaException(request.getPlagaId()));
        Agroquimico agroquimico = agroquimicoRepository.findByIdAndActivoTrue(request.getAgroquimicoId())
                .orElseThrow(() -> new AgroquimicoNoEncontradoException(request.getAgroquimicoId()));
        Cultivo cultivo = cultivoRepository.findByIdAndActivoTrue(request.getCultivoId())
                .orElseThrow(() -> new CultivoNoEncontradoException(request.getCultivoId()));

        // RN-TRA-04: Coherencia de plaga y químico
        validarCoherencia(plaga, agroquimico);

        tratamiento.setPlaga(plaga);
        tratamiento.setAgroquimico(agroquimico);
        tratamiento.setCultivo(cultivo);
        tratamiento.setDosisPorHectarea(request.getDosisPorHectarea());
        tratamiento.setUnidadMedida(request.getUnidadMedida());
    }

    private void validarCoherencia(Plaga plaga, Agroquimico agroquimico) {
        TipoPlaga tipoPlaga = plaga.getTipoPlaga();
        TipoAgroquimico tipoAgro = agroquimico.getTipo();

        if (tipoAgro == TipoAgroquimico.FERTILIZANTE) {
            throw new ReglaNegocioException("Un fertilizante no combate ninguna plaga");
        }

        if (tipoPlaga == TipoPlaga.INSECTO && tipoAgro != TipoAgroquimico.INSECTICIDA) {
            throw new ReglaNegocioException("Para plagas de tipo INSECTO debe utilizarse un INSECTICIDA");
        }
        if (tipoPlaga == TipoPlaga.HONGO && tipoAgro != TipoAgroquimico.FUNGICIDA) {
            throw new ReglaNegocioException("Para plagas de tipo HONGO debe utilizarse un FUNGICIDA");
        }
        if (tipoPlaga == TipoPlaga.MALEZA && tipoAgro != TipoAgroquimico.HERBICIDA) {
            throw new ReglaNegocioException("Para plagas de tipo MALEZA debe utilizarse un HERBICIDA");
        }
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
