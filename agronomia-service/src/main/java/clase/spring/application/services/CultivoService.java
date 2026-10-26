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
        return cultivoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CultivoResponse obtenerPorId(Long id) {
        Cultivo cultivo = cultivoRepository.findById(id)
                .orElseThrow(() -> new CultivoNoEncontradoException(id));
        return toResponse(cultivo);
    }

    public CultivoResponse crear(CultivoRequest request) {
        if (cultivoRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new ReglaNegocioException("Ya existe un cultivo con el nombre: " + request.getNombre());
        }
        Cultivo cultivo = Cultivo.builder()
                .nombre(request.getNombre())
                .suelosAptos(request.getSuelosAptos())
                .labores(request.getLabores() != null ? request.getLabores() : new ArrayList<>())
                .build();
        Cultivo guardado = cultivoRepository.save(cultivo);
        return toResponse(guardado);
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
