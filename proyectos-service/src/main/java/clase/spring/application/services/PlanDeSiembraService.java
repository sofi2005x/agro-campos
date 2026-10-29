package clase.spring.application.services;

import clase.spring.application.dtos.request.LaborPatchRequest;
import clase.spring.application.dtos.request.PlanPatchRequest;
import clase.spring.application.dtos.request.PlanRequest;
import clase.spring.application.dtos.response.LaborResponse;
import clase.spring.application.dtos.response.PlanResponse;
import clase.spring.domain.exceptions.PlanNoEncontradoException;
import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.exceptions.ServicioNoDisponibleException;
import clase.spring.domain.models.CultivoInfo;
import clase.spring.domain.models.EstadoPlan;
import clase.spring.domain.models.Labor;
import clase.spring.domain.models.LoteInfo;
import clase.spring.domain.models.PlanDeSiembra;
import clase.spring.domain.models.PronosticoClima;
import clase.spring.domain.models.TipoLabor;
import clase.spring.domain.models.TratamientoInfo;
import clase.spring.domain.ports.AgronomiaPort;
import clase.spring.domain.ports.CamposPort;
import clase.spring.domain.ports.ClimaPort;
import clase.spring.domain.ports.PlanRepository;
import clase.spring.domain.services.PlanDomainService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de aplicación: orquesta cada caso de uso. Obtiene los datos a través de los ports (persistencia y otros
 * servicios) y delega las decisiones en el dominio (PlanDeSiembra y PlanDomainService); no decide reglas por su cuenta.
 * Depende solo de interfaces del dominio: no conoce a RestClient, a JPA ni a Open-Meteo.
 */
@AllArgsConstructor
@Service
public class PlanDeSiembraService {

    private static final Logger log = LoggerFactory.getLogger(PlanDeSiembraService.class);

    private final PlanRepository planRepository;
    private final CamposPort camposPort;
    private final AgronomiaPort agronomiaPort;
    private final ClimaPort climaPort;
    private final PlanDomainService planDomainService;


    @Transactional(readOnly = true)
    public List<PlanResponse> listar(EstadoPlan estado) {
        List<PlanDeSiembra> planes = estado == null ? planRepository.buscarTodos() : planRepository.buscarPorEstado(estado);
        return planes.stream().map(plan -> toResponse(plan, List.of())).toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse obtenerPorId(Long id) {
        return toResponse(buscarPlan(id), List.of());
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listarPorLote(Long loteId) {
        return planRepository.buscarPorLote(loteId).stream()
                .map(plan -> toResponse(plan, List.of()))
                .toList();
    }

    /** Crea un plan de siembra aplicando RN-LAB-01 (aptitud de suelo) y RN-LAB-02 (exclusividad del lote). */
    @Transactional
    public PlanResponse crear(PlanRequest request) {
        LoteInfo lote = camposPort.obtenerLote(request.getCampoId(), request.getLoteId());
        CultivoInfo cultivo = agronomiaPort.obtenerCultivo(request.getCultivoId());

        planDomainService.validarAptitudSuelo(lote, cultivo);
        planDomainService.validarLoteDisponible(lote.id(), planRepository.buscarPorLote(lote.id()));

        PlanDeSiembra plan = PlanDeSiembra.crear(lote.campoId(), lote.id(), cultivo.id(), cultivo.nombre(),
                request.getFechaSiembraPrevista(), cultivo.labores());

        return toResponse(planRepository.guardar(plan), List.of());
    }

    @Transactional
    public PlanResponse actualizarEstado(Long id, PlanPatchRequest request) {
        PlanDeSiembra plan = buscarPlan(id);
        if (request.getEstado() != EstadoPlan.CANCELADO) {
            throw new ReglaNegocioException("El estado de un plan solo puede modificarse a CANCELADO; "
                    + "los demás estados se derivan de sus labores");
        }
        plan.cancelar();
        return toResponse(planRepository.guardar(plan), List.of());
    }

    /**
     * Cambia el estado de una labor. Iniciarla válida RN-LAB-03 (secuencia) y, si es una fumigación,
     * RN-LAB-05 (clima). Completarla puede cerrar el plan (RN-LAB-04).
     */
    @Transactional
    public PlanResponse actualizarLabor(Long planId, Long laborId, LaborPatchRequest request) {
        PlanDeSiembra plan = buscarPlan(planId);
        Labor labor = plan.buscarLabor(laborId);
        List<String> advertencias = new ArrayList<>();

        switch (request.getEstado()) {
            case EN_EJECUCION -> {
                plan.validarPuedeIniciar(labor); // RN-LAB-03, antes de gastar llamadas remotas
                if (labor.getTipo() == TipoLabor.FUMIGAR) {
                    validarCondicionesClimaticas(plan, request.getTratamientoId(), advertencias);
                }
                plan.iniciarLabor(labor, request.getTratamientoId());
            }
            case COMPLETADA -> plan.completarLabor(labor);
            case PENDIENTE -> throw new ReglaNegocioException("Una labor no puede volver a PENDIENTE");
        }

        return toResponse(planRepository.guardar(plan), advertencias);
    }

    /**
     * RN-LAB-05. Si el clima no se puede consultar (proveedor caído, lote sin coordenadas) la fumigación se
     * habilita igual y se informa en la respuesta, porque un corte externo no debe frenar la operación del campo.
     */
    private void validarCondicionesClimaticas(PlanDeSiembra plan, Long tratamientoId, List<String> advertencias) {
        if (tratamientoId == null) {
            throw new ReglaNegocioException("Para iniciar una fumigación debe indicar el tratamientoId");
        }
        TratamientoInfo tratamiento = agronomiaPort.obtenerTratamiento(tratamientoId);
        planDomainService.validarTratamientoDelCultivo(tratamiento, plan.getCultivoId());

        LoteInfo lote = camposPort.obtenerLote(plan.getCampoId(), plan.getLoteId());
        if (!lote.tieneCoordenadas()) {
            advertencias.add("El lote " + lote.id() + " no tiene coordenadas cargadas: no se pudo validar el clima");
            return;
        }

        PronosticoClima pronostico;
        try {
            pronostico = climaPort.obtenerPronostico(lote.latitud(), lote.longitud(), tratamiento.horasLibresLluvia());
        } catch (ServicioNoDisponibleException ex) {
            log.warn("No se pudo validar el clima para el lote {}: {}", lote.id(), ex.getMessage());
            advertencias.add("No se pudo validar el clima (" + ex.getServicio() + " no disponible): "
                    + "la fumigación se habilitó sin control climático");
            return;
        }
        planDomainService.validarCondicionesFumigacion(tratamiento, pronostico);
    }

    private PlanDeSiembra buscarPlan(Long id) {
        return planRepository.buscarPorId(id).orElseThrow(() -> new PlanNoEncontradoException(id));
    }

    private PlanResponse toResponse(PlanDeSiembra plan, List<String> advertencias) {
        return PlanResponse.builder()
                .id(plan.getId())
                .campoId(plan.getCampoId())
                .loteId(plan.getLoteId())
                .cultivoId(plan.getCultivoId())
                .cultivoNombre(plan.getCultivoNombre())
                .estado(plan.getEstado())
                .fechaSiembraPrevista(plan.getFechaSiembraPrevista())
                .fechaCreacion(plan.getFechaCreacion())
                .labores(plan.getLabores().stream().map(this::toResponse).toList())
                .advertencias(advertencias)
                .build();
    }

    private LaborResponse toResponse(Labor labor) {
        return LaborResponse.builder()
                .id(labor.getId())
                .orden(labor.getOrden())
                .nombre(labor.getNombre())
                .tipo(labor.getTipo())
                .estado(labor.getEstado())
                .tratamientoId(labor.getTratamientoId())
                .fechaInicio(labor.getFechaInicio())
                .fechaFin(labor.getFechaFin())
                .build();
    }
}
