package clase.spring.application.services;

import clase.spring.application.dtos.request.CampoRequestDTO;
import clase.spring.application.dtos.request.LoteRequestDTO;
import clase.spring.application.dtos.response.CampoResponseDTO;
import clase.spring.application.dtos.response.LoteResponseDTO;
import clase.spring.domain.exceptions.BusinessRuleException;
import clase.spring.domain.exceptions.ResourceNotFoundException;
import clase.spring.domain.models.Campo;
import clase.spring.domain.models.Lote;
import clase.spring.infrastructure.repositories.CampoRepository;
import clase.spring.infrastructure.repositories.LoteRepository;
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
        return campoRepository.findByActivoTrue().stream()
                .map(CampoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CampoResponseDTO buscarPorId(Long id) {
        Campo campo = campoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campo no encontrado con ID: " + id));
        return CampoResponseDTO.fromEntity(campo);
    }

    @Transactional
    public CampoResponseDTO crearCampo(CampoRequestDTO request) {
        // RN-CAM-01: No duplicar nombre de campo
        if (campoRepository.existsByNombreIgnoreCaseAndActivoTrue(request.nombre())) {
            throw new BusinessRuleException("Ya existe un campo con el nombre: " + request.nombre());
        }

        Campo campo = Campo.builder()
                .nombre(request.nombre())
                .superficieTotalHas(request.superficieTotalHas())
                .activo(true)
                .build();

        return CampoResponseDTO.fromEntity(campoRepository.save(campo));
    }

    @Transactional
    public CampoResponseDTO actualizarCampo(Long id, CampoRequestDTO request) {
        Campo campo = campoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campo no encontrado con ID: " + id));

        // RN-CAM-01: No duplicar nombre con otro campo existente
        if (campoRepository.existsByNombreIgnoreCaseAndIdNotAndActivoTrue(request.nombre(), id)) {
            throw new BusinessRuleException("Ya existe otro campo con el nombre: " + request.nombre());
        }

        // RN-CAM-02 (en actualización): la superficie total no puede ser menor a la suma de sus lotes activos actuales
        double superficieLotesActuales = campo.getLotes().stream()
                .filter(l -> Boolean.TRUE.equals(l.getActivo()))
                .mapToDouble(Lote::getHectareas)
                .sum();

        if (request.superficieTotalHas() < superficieLotesActuales) {
            throw new BusinessRuleException(String.format(
                    "Superficie inválida: la nueva superficie (%.2f has) no puede ser menor a la ocupada por sus lotes activos (%.2f has).",
                    request.superficieTotalHas(), superficieLotesActuales
            ));
        }

        campo.setNombre(request.nombre());
        campo.setSuperficieTotalHas(request.superficieTotalHas());

        return CampoResponseDTO.fromEntity(campoRepository.save(campo));
    }

    @Transactional
    public void eliminarCampo(Long id) {
        Campo campo = campoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campo no encontrado con ID: " + id));

        // Baja lógica del campo y de todos sus lotes asociados
        campo.setActivo(false);
        if (campo.getLotes() != null) {
            campo.getLotes().forEach(lote -> lote.setActivo(false));
        }
        campoRepository.save(campo);
    }

    @Transactional
    public LoteResponseDTO agregarLoteACampo(Long campoId, LoteRequestDTO request) {
        Campo campo = campoRepository.findByIdAndActivoTrue(campoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campo no encontrado con ID: " + campoId));

        // RN-CAM-03: No duplicar nro de lote en el mismo campo
        if (loteRepository.existsByCampoIdAndNroLoteAndActivoTrue(campoId, request.nroLote())) {
            throw new BusinessRuleException("El lote N° " + request.nroLote() + " ya existe en este campo.");
        }

        // RN-CAM-02: Control de superficie (la suma de has de lotes activos no puede exceder el campo)
        double superficieLotesActuales = campo.getLotes().stream()
                .filter(l -> Boolean.TRUE.equals(l.getActivo()))
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
                .activo(true)
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
        return loteRepository.findByCampoIdAndActivoTrue(campoId).stream()
                .map(LoteResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public LoteResponseDTO buscarLotePorId(Long campoId, Long loteId) {
        if (!campoRepository.existsById(campoId)) {
            throw new ResourceNotFoundException("Campo no encontrado con ID: " + campoId);
        }
        Lote lote = loteRepository.findByIdAndCampoIdAndActivoTrue(loteId, campoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lote no encontrado con ID: " + loteId + " en el campo: " + campoId));
        return LoteResponseDTO.fromEntity(lote);
    }

    @Transactional
    public LoteResponseDTO actualizarLote(Long campoId, Long loteId, LoteRequestDTO request) {
        Campo campo = campoRepository.findByIdAndActivoTrue(campoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campo no encontrado con ID: " + campoId));

        Lote lote = loteRepository.findByIdAndCampoIdAndActivoTrue(loteId, campoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lote no encontrado con ID: " + loteId + " en el campo: " + campoId));

        // RN-CAM-03: No duplicar número de lote con otro lote del mismo campo
        if (loteRepository.existsByCampoIdAndNroLoteAndIdNotAndActivoTrue(campoId, request.nroLote(), loteId)) {
            throw new BusinessRuleException("El lote N° " + request.nroLote() + " ya existe en este campo.");
        }

        // RN-CAM-02: Control de superficie sumando los demás lotes activos
        double superficieOtrosLotes = campo.getLotes().stream()
                .filter(l -> Boolean.TRUE.equals(l.getActivo()) && !l.getId().equals(loteId))
                .mapToDouble(Lote::getHectareas)
                .sum();

        if (superficieOtrosLotes + request.hectareas() > campo.getSuperficieTotalHas()) {
            throw new BusinessRuleException(String.format(
                    "Superficie excedida: el campo tiene %.2f has, los demás lotes ocupan %.2f has y se intentan asignar %.2f has.",
                    campo.getSuperficieTotalHas(), superficieOtrosLotes, request.hectareas()
            ));
        }

        lote.setNroLote(request.nroLote());
        lote.setHectareas(request.hectareas());
        lote.setLatitud(request.latitud());
        lote.setLongitud(request.longitud());
        lote.setTipoSuelo(request.tipoSuelo());

        return LoteResponseDTO.fromEntity(loteRepository.save(lote));
    }

    @Transactional
    public void eliminarLote(Long campoId, Long loteId) {
        if (!campoRepository.existsById(campoId)) {
            throw new ResourceNotFoundException("Campo no encontrado con ID: " + campoId);
        }
        Lote lote = loteRepository.findByIdAndCampoIdAndActivoTrue(loteId, campoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lote no encontrado con ID: " + loteId + " en el campo: " + campoId));

        // Baja lógica del lote
        lote.setActivo(false);
        loteRepository.save(lote);
    }
}
