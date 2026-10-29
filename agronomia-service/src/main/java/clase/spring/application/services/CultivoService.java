package clase.spring.application.services;

import clase.spring.application.dtos.request.CultivoRequest;
import clase.spring.application.dtos.response.CultivoResponse;
import clase.spring.domain.exceptions.CultivoNoEncontradoException;
import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.models.Cultivo;
import clase.spring.infrastructure.repositories.CultivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CultivoService {

    private final CultivoRepository cultivoRepository;

    public List<CultivoResponse> listar() {
        return cultivoRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CultivoResponse obtenerPorId(Long id) {
        Cultivo cultivo = cultivoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new CultivoNoEncontradoException(id));
        return toResponse(cultivo);
    }

    public CultivoResponse crear(CultivoRequest request) {
        if (cultivoRepository.existsByNombreIgnoreCaseAndActivoTrue(request.getNombre())) {
            throw new ReglaNegocioException("Ya existe un cultivo con el nombre: " + request.getNombre());
        }
        Cultivo cultivo = Cultivo.builder()
                .nombre(request.getNombre())
                .suelosAptos(request.getSuelosAptos())
                .labores(request.getLabores() != null ? request.getLabores() : new ArrayList<>())
                .activo(true)
                .build();
        Cultivo guardado = cultivoRepository.save(cultivo);
        return toResponse(guardado);
    }

    public CultivoResponse actualizar(Long id, CultivoRequest request) {
        Cultivo cultivo = cultivoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new CultivoNoEncontradoException(id));

        if (cultivoRepository.existsByNombreIgnoreCaseAndIdNotAndActivoTrue(request.getNombre(), id)) {
            throw new ReglaNegocioException("Ya existe otro cultivo con el nombre: " + request.getNombre());
        }

        cultivo.setNombre(request.getNombre());
        cultivo.setSuelosAptos(request.getSuelosAptos());
        cultivo.setLabores(request.getLabores() != null ? request.getLabores() : new ArrayList<>());

        return toResponse(cultivoRepository.save(cultivo));
    }

    public void eliminar(Long id) {
        Cultivo cultivo = cultivoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new CultivoNoEncontradoException(id));
        cultivo.setActivo(false);
        cultivoRepository.save(cultivo);
    }

    private CultivoResponse toResponse(Cultivo cultivo) {
        return CultivoResponse.builder()
                .id(cultivo.getId())
                .nombre(cultivo.getNombre())
                .suelosAptos(cultivo.getSuelosAptos())
                .labores(cultivo.getLabores())
                .build();
    }
}
