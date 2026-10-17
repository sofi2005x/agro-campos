package clase.spring.services;

import clase.spring.dtos.request.CampoRequestDTO;
import clase.spring.dtos.request.LoteRequestDTO;
import clase.spring.dtos.response.CampoResponseDTO;
import clase.spring.dtos.response.LoteResponseDTO;
import clase.spring.exceptions.BusinessRuleException;
import clase.spring.exceptions.ResourceNotFoundException;
import clase.spring.models.Campo;
import clase.spring.models.Lote;
import clase.spring.repositories.CampoRepository;
import clase.spring.repositories.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampoService {

    private final CampoRepository campoRepository;
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public List<CampoResponseDTO> listarTodos() {
        return campoRepository.findAll().stream()
                .map(CampoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CampoResponseDTO buscarPorId(Long id) {
        Campo campo = campoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campo no encontrado con ID: " + id));
        return CampoResponseDTO.fromEntity(campo);
    }

    @Transactional
    public CampoResponseDTO crearCampo(CampoRequestDTO request) {
        // Regla: No duplicar nombre de campo
        if (campoRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new BusinessRuleException("Ya existe un campo con el nombre: " + request.nombre());
        }

        Campo campo = Campo.builder()
                .nombre(request.nombre())
                .superficieTotalHas(request.superficieTotalHas())
                .build();

        return CampoResponseDTO.fromEntity(campoRepository.save(campo));
    }

    @Transactional
    public LoteResponseDTO agregarLoteACampo(Long campoId, LoteRequestDTO request) {
        Campo campo = campoRepository.findById(campoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campo no encontrado con ID: " + campoId));

        // Regla: No duplicar nro de lote en el mismo campo
        if (loteRepository.existsByCampoIdAndNroLote(campoId, request.nroLote())) {
            throw new BusinessRuleException("El lote N° " + request.nroLote() + " ya existe en este campo.");
        }

        // Regla 6: Control de superficie (la suma de has de los lotes no puede exceder el campo)
        double superficieLotesActuales = campo.getLotes().stream()
                .mapToDouble(Lote::getHectareas)
                .sum();

        if (superficieLotesActuales + request.hectareas() > campo.getSuperficieTotalHas()) {
            throw new BusinessRuleException(String.format(
                    "Superficie excedida: el campo tiene %.2f has, lotes ocupan %.2f has y se intentan agregar %.2f has.",
                    campo.getSuperficieTotalHas(), superficieLotesActuales, request.hectareas()
            ));
        }

        Lote lote = Lote.builder()
                .nroLote(request.nroLote())
                .hectareas(request.hectareas())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .tipoSuelo(request.tipoSuelo())
                .campo(campo)
                .build();

        Lote saved = loteRepository.save(lote);
        campo.getLotes().add(saved);

        return LoteResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listarLotesDeCampo(Long campoId) {
        if (!campoRepository.existsById(campoId)) {
            throw new ResourceNotFoundException("Campo no encontrado con ID: " + campoId);
        }
        return loteRepository.findByCampoId(campoId).stream()
                .map(LoteResponseDTO::fromEntity)
                .toList();
    }
}
