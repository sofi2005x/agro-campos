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
        return plagaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PlagaResponse obtenerPorId(Long id) {
        Plaga plaga = plagaRepository.findById(id)
                .orElseThrow(() -> new PlagaNoEncontradaException(id));
        return toResponse(plaga);
    }

    public PlagaResponse crear(PlagaRequest request) {
        if (plagaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new ReglaNegocioException("Ya existe una plaga con el nombre: " + request.getNombre());
        }
        Plaga plaga = Plaga.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .tipoPlaga(request.getTipoPlaga())
                .build();
        Plaga guardado = plagaRepository.save(plaga);
        return toResponse(guardado);
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
