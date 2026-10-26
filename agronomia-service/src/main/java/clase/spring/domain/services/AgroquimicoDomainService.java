package clase.spring.domain.services;

import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.models.Agroquimico;
import clase.spring.infrastructure.repositories.AgroquimicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgroquimicoDomainService {

    private final AgroquimicoRepository agroquimicoRepository;

    public void validarUnicidadNombreComercial(String nombreComercial) {
        if (agroquimicoRepository.existsByNombreComercialIgnoreCase(nombreComercial)) {
            throw new ReglaNegocioException("Ya existe un agroquímico con el nombre comercial: " + nombreComercial);
        }
    }

    public void validarUnicidadNombreComercial(String nombreComercial, Long idExcluir) {
        agroquimicoRepository.findByNombreComercialIgnoreCase(nombreComercial)
                .ifPresent(a -> {
                    if (!a.getId().equals(idExcluir)) {
                        throw new ReglaNegocioException("Ya existe un agroquímico con el nombre comercial: " + nombreComercial);
                    }
                });
    }
}
