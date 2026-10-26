package clase.spring.application.services;

import clase.spring.application.dtos.request.AgroquimicoRequest;
import clase.spring.application.dtos.response.AgroquimicoResponse;
import clase.spring.domain.exceptions.AgroquimicoNoEncontradoException;
import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.models.Agroquimico;
import clase.spring.domain.models.TipoAgroquimico;
import clase.spring.domain.services.AgroquimicoDomainService;
import clase.spring.infrastructure.repositories.AgroquimicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AgroquimicoService {

    private final AgroquimicoRepository agroquimicoRepository;
    private final AgroquimicoDomainService agroquimicoDomainService;

    public List<AgroquimicoResponse> listar() {
        return agroquimicoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AgroquimicoResponse obtenerPorId(Long id) {
        Agroquimico agroquimico = agroquimicoRepository.findById(id)
                .orElseThrow(() -> new AgroquimicoNoEncontradoException(id));
        return toResponse(agroquimico);
    }

    public AgroquimicoResponse crear(AgroquimicoRequest request) {
        agroquimicoDomainService.validarUnicidadNombreComercial(request.getNombreComercial());
        if (request.getTipo() == TipoAgroquimico.FERTILIZANTE) {
            // RN-TRA-04: Un FERTILIZANTE no combate ninguna plaga; coherencia se valida en Tratamiento
        }
        Agroquimico agroquimico = Agroquimico.builder()
                .nombreComercial(request.getNombreComercial())
                .principioActivo(request.getPrincipioActivo())
                .tipo(request.getTipo())
                .vientoMaximoPermitidoKmH(request.getVientoMaximoPermitidoKmH() != null ? request.getVientoMaximoPermitidoKmH() : 15.0)
                .horasLibresDeLluviaRequeridas(request.getHorasLibresDeLluviaRequeridas() != null ? request.getHorasLibresDeLluviaRequeridas() : 4)
                .build();
        Agroquimico guardado = agroquimicoRepository.save(agroquimico);
        return toResponse(guardado);
    }

    private AgroquimicoResponse toResponse(Agroquimico agroquimico) {
        return AgroquimicoResponse.builder()
                .id(agroquimico.getId())
                .nombreComercial(agroquimico.getNombreComercial())
                .principioActivo(agroquimico.getPrincipioActivo())
                .tipo(agroquimico.getTipo())
                .vientoMaximoPermitidoKmH(agroquimico.getVientoMaximoPermitidoKmH())
                .horasLibresDeLluviaRequeridas(agroquimico.getHorasLibresDeLluviaRequeridas())
                .build();
    }
}
