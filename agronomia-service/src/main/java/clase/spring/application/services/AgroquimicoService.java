package clase.spring.application.services;

import clase.spring.application.dtos.request.AgroquimicoRequest;
import clase.spring.application.dtos.response.AgroquimicoResponse;
import clase.spring.domain.exceptions.AgroquimicoNoEncontradoException;
import clase.spring.domain.models.Agroquimico;
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
        return agroquimicoRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AgroquimicoResponse obtenerPorId(Long id) {
        Agroquimico agroquimico = agroquimicoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new AgroquimicoNoEncontradoException(id));
        return toResponse(agroquimico);
    }

    public AgroquimicoResponse crear(AgroquimicoRequest request) {
        agroquimicoDomainService.validarUnicidadNombreComercial(request.getNombreComercial());
        Agroquimico agroquimico = Agroquimico.builder()
                .nombreComercial(request.getNombreComercial())
                .principioActivo(request.getPrincipioActivo())
                .tipo(request.getTipo())
                .vientoMaximoPermitidoKmH(request.getVientoMaximoPermitidoKmH() != null ? request.getVientoMaximoPermitidoKmH() : 15.0)
                .horasLibresDeLluviaRequeridas(request.getHorasLibresDeLluviaRequeridas() != null ? request.getHorasLibresDeLluviaRequeridas() : 4)
                .activo(true)
                .build();
        Agroquimico guardado = agroquimicoRepository.save(agroquimico);
        return toResponse(guardado);
    }

    public AgroquimicoResponse actualizar(Long id, AgroquimicoRequest request) {
        Agroquimico agroquimico = agroquimicoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new AgroquimicoNoEncontradoException(id));

        agroquimicoDomainService.validarUnicidadNombreComercial(request.getNombreComercial(), id);

        agroquimico.setNombreComercial(request.getNombreComercial());
        agroquimico.setPrincipioActivo(request.getPrincipioActivo());
        agroquimico.setTipo(request.getTipo());
        agroquimico.setVientoMaximoPermitidoKmH(request.getVientoMaximoPermitidoKmH() != null ? request.getVientoMaximoPermitidoKmH() : 15.0);
        agroquimico.setHorasLibresDeLluviaRequeridas(request.getHorasLibresDeLluviaRequeridas() != null ? request.getHorasLibresDeLluviaRequeridas() : 4);

        return toResponse(agroquimicoRepository.save(agroquimico));
    }

    public void eliminar(Long id) {
        Agroquimico agroquimico = agroquimicoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new AgroquimicoNoEncontradoException(id));
        agroquimico.setActivo(false);
        agroquimicoRepository.save(agroquimico);
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
