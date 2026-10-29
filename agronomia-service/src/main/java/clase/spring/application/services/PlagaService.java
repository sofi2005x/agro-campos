package clase.spring.application.services;

import clase.spring.application.dtos.request.PlagaRequest;
import clase.spring.application.dtos.response.PlagaResponse;
import clase.spring.domain.exceptions.PlagaNoEncontradaException;
import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.models.Plaga;
import clase.spring.infrastructure.repositories.PlagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlagaService {

    private final PlagaRepository plagaRepository;

    public List<PlagaResponse> listar() {
        return plagaRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PlagaResponse obtenerPorId(Long id) {
        Plaga plaga = plagaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new PlagaNoEncontradaException(id));
        return toResponse(plaga);
    }

    public PlagaResponse crear(PlagaRequest request) {
        if (plagaRepository.existsByNombreIgnoreCaseAndActivoTrue(request.getNombre())) {
            throw new ReglaNegocioException("Ya existe una plaga con el nombre: " + request.getNombre());
        }
        Plaga plaga = Plaga.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .tipoPlaga(request.getTipoPlaga())
                .activo(true)
                .build();
        Plaga guardado = plagaRepository.save(plaga);
        return toResponse(guardado);
    }

    public PlagaResponse actualizar(Long id, PlagaRequest request) {
        Plaga plaga = plagaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new PlagaNoEncontradaException(id));

        if (plagaRepository.existsByNombreIgnoreCaseAndIdNotAndActivoTrue(request.getNombre(), id)) {
            throw new ReglaNegocioException("Ya existe otra plaga con el nombre: " + request.getNombre());
        }

        plaga.setNombre(request.getNombre());
        plaga.setDescripcion(request.getDescripcion());
        plaga.setTipoPlaga(request.getTipoPlaga());

        return toResponse(plagaRepository.save(plaga));
    }

    public void eliminar(Long id) {
        Plaga plaga = plagaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new PlagaNoEncontradaException(id));
        plaga.setActivo(false);
        plagaRepository.save(plaga);
    }

    private PlagaResponse toResponse(Plaga plaga) {
        return PlagaResponse.builder()
                .id(plaga.getId())
                .nombre(plaga.getNombre())
                .descripcion(plaga.getDescripcion())
                .tipoPlaga(plaga.getTipoPlaga())
                .build();
    }
}
