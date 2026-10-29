package clase.spring.application.services;

import clase.spring.application.dtos.request.LaborPatchRequest;
import clase.spring.application.dtos.request.PlanPatchRequest;
import clase.spring.application.dtos.request.PlanRequest;
import clase.spring.application.dtos.response.LaborResponse;
import clase.spring.application.dtos.response.PlanResponse;
import clase.spring.domain.exceptions.ReferenciaNoEncontradaException;
import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.exceptions.ServicioNoDisponibleException;
import clase.spring.domain.models.*;
import clase.spring.domain.ports.AgronomiaPort;
import clase.spring.domain.ports.CamposPort;
import clase.spring.domain.ports.ClimaPort;
import clase.spring.domain.ports.PlanRepository;
import clase.spring.domain.services.PlanDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba los casos de uso con implementaciones en memoria de los ports: sin base de datos, sin red
 * y sin contexto de Spring. Es la ventaja de que el servicio dependa solo de interfaces del dominio.
 */
class PlanDeSiembraServiceTest {

    // ---------- implementaciones en memoria de los ports ----------

    static class RepositorioEnMemoria implements PlanRepository {
        final Map<Long, PlanDeSiembra> planes = new LinkedHashMap<>();
        private long proximoId = 1;
        private long proximaLabor = 1;

        @Override
        public PlanDeSiembra guardar(PlanDeSiembra plan) {
            try {
                if (plan.getId() == null) {
                    asignarId(PlanDeSiembra.class, plan, proximoId++);
                    for (Labor labor : plan.getLabores()) {
                        asignarId(Labor.class, labor, proximaLabor++);
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
            planes.put(plan.getId(), plan);
            return plan;
        }

        private void asignarId(Class<?> tipo, Object entidad, long id) throws ReflectiveOperationException {
            Field campo = tipo.getDeclaredField("id");
            campo.setAccessible(true);
            campo.set(entidad, id);
        }

        @Override
        public Optional<PlanDeSiembra> buscarPorId(Long id) {
            return Optional.ofNullable(planes.get(id));
        }

        @Override
        public List<PlanDeSiembra> buscarTodos() {
            return new ArrayList<>(planes.values());
        }

        @Override
        public List<PlanDeSiembra> buscarPorEstado(EstadoPlan estado) {
            return planes.values().stream().filter(p -> p.getEstado() == estado).toList();
        }

        @Override
        public List<PlanDeSiembra> buscarPorLote(Long loteId) {
            return planes.values().stream().filter(p -> p.getLoteId().equals(loteId)).toList();
        }
    }

    static class CamposFalso implements CamposPort {
        final Map<Long, LoteInfo> lotes = new HashMap<>();

        @Override
        public LoteInfo obtenerLote(Long campoId, Long loteId) {
            LoteInfo lote = lotes.get(loteId);
            if (lote == null) {
                throw new ReferenciaNoEncontradaException("Lote no encontrado con ID: " + loteId);
            }
            return lote;
        }
    }

    static class AgronomiaFalsa implements AgronomiaPort {
        final Map<Long, CultivoInfo> cultivos = new HashMap<>();
        final Map<Long, TratamientoInfo> tratamientos = new HashMap<>();

        @Override
        public CultivoInfo obtenerCultivo(Long cultivoId) {
            CultivoInfo cultivo = cultivos.get(cultivoId);
            if (cultivo == null) {
                throw new ReferenciaNoEncontradaException("Cultivo no encontrado con ID: " + cultivoId);
            }
            return cultivo;
        }

        @Override
        public TratamientoInfo obtenerTratamiento(Long tratamientoId) {
            TratamientoInfo tratamiento = tratamientos.get(tratamientoId);
            if (tratamiento == null) {
                throw new ReferenciaNoEncontradaException("Tratamiento no encontrado con ID: " + tratamientoId);
            }
            return tratamiento;
        }
    }

    static class ClimaFalso implements ClimaPort {
        PronosticoClima respuesta;
        boolean caido;
        int llamadas;

        @Override
        public PronosticoClima obtenerPronostico(double latitud, double longitud, int horas) {
            llamadas++;
            if (caido) {
                throw new ServicioNoDisponibleException("Open-Meteo", "timeout");
            }
            return respuesta;
        }
    }

    // ---------- fixture ----------

    private RepositorioEnMemoria repositorio;
    private CamposFalso campos;
    private AgronomiaFalsa agronomia;
    private ClimaFalso clima;
    private PlanDeSiembraService service;

    private static final long CAMPO = 1L;
    private static final long LOTE = 10L;
    private static final long SOJA = 5L;
    private static final long TRATAMIENTO = 7L;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioEnMemoria();
        campos = new CamposFalso();
        agronomia = new AgronomiaFalsa();
        clima = new ClimaFalso();
        service = new PlanDeSiembraService(repositorio, campos, agronomia, clima, new PlanDomainService());

        campos.lotes.put(LOTE, new LoteInfo(LOTE, CAMPO, 1, -31.42, -64.18, "TIPO_2"));
        agronomia.cultivos.put(SOJA, new CultivoInfo(SOJA, "Soja", Set.of("TIPO_1", "TIPO_2"),
                List.of("Arar", "Sembrar", "Fumigar", "Cosechar")));
        agronomia.tratamientos.put(TRATAMIENTO, new TratamientoInfo(TRATAMIENTO, SOJA, "Coragen", 15.0, 4));
        clima.respuesta = climaFavorable();
    }

    private PronosticoClima climaFavorable() {
        return pronostico(8.0, 0.0);
    }

    private PronosticoClima pronostico(double viento, double lluvia) {
        LocalDateTime base = LocalDateTime.of(2026, 11, 2, 12, 0);
        List<HoraPronostico> horas = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            horas.add(new HoraPronostico(base.plusHours(i), viento, lluvia));
        }
        return new PronosticoClima(horas);
    }

    private PlanResponse crearPlan() {
        return service.crear(PlanRequest.builder()
                .campoId(CAMPO).loteId(LOTE).cultivoId(SOJA)
                .fechaSiembraPrevista(LocalDate.now().plusDays(10))
                .build());
    }

    private PlanResponse cambiarLabor(PlanResponse plan, int orden, EstadoLabor estado, Long tratamientoId) {
        LaborResponse labor = plan.getLabores().get(orden - 1);
        return service.actualizarLabor(plan.getId(), labor.getId(),
                LaborPatchRequest.builder().estado(estado).tratamientoId(tratamientoId).build());
    }

    private PlanResponse hacerLabor(PlanResponse plan, int orden) {
        cambiarLabor(plan, orden, EstadoLabor.EN_EJECUCION, TRATAMIENTO);
        return cambiarLabor(plan, orden, EstadoLabor.COMPLETADA, null);
    }

    // ---------- crear: RN-LAB-01 y RN-LAB-02 ----------

    @Test
    void creaUnPlanPlanificadoConLasLaboresDelCultivo() {
        PlanResponse plan = crearPlan();

        assertEquals(EstadoPlan.PLANIFICADO, plan.getEstado());
        assertEquals("Soja", plan.getCultivoNombre());
        assertEquals(4, plan.getLabores().size());
        assertEquals("Arar", plan.getLabores().get(0).getNombre());
        assertEquals(1, repositorio.planes.size());
    }

    @Test
    void rechazaUnCultivoNoAptoParaElSueloDelLote() {
        campos.lotes.put(LOTE, new LoteInfo(LOTE, CAMPO, 1, -31.42, -64.18, "TIPO_5"));

        assertThrows(ReglaNegocioException.class, this::crearPlan);
        assertTrue(repositorio.planes.isEmpty());
    }

    @Test
    void rechazaUnSegundoPlanActivoEnElMismoLote() {
        crearPlan();

        assertThrows(ReglaNegocioException.class, this::crearPlan);
        assertEquals(1, repositorio.planes.size());
    }

    @Test
    void propagaLaFaltaDeLoteComoReferenciaNoEncontrada() {
        campos.lotes.clear();

        assertThrows(ReferenciaNoEncontradaException.class, this::crearPlan);
        assertTrue(repositorio.planes.isEmpty());
    }

    @Test
    void rechazaUnCultivoSinLaboresDefinidas() {
        agronomia.cultivos.put(SOJA, new CultivoInfo(SOJA, "Soja", Set.of("TIPO_2"), List.of()));

        assertThrows(ReglaNegocioException.class, this::crearPlan);
    }

    // ---------- lote libre al cerrar o cancelar (RN-LAB-02 + RN-LAB-04) ----------

    @Test
    void alCompletarLaCosechaElPlanSeFinalizaYElLoteQuedaLibre() {
        PlanResponse plan = crearPlan();
        hacerLabor(plan, 1);
        hacerLabor(plan, 2);
        hacerLabor(plan, 3);
        cambiarLabor(plan, 4, EstadoLabor.EN_EJECUCION, null);

        PlanResponse cerrado = cambiarLabor(plan, 4, EstadoLabor.COMPLETADA, null);

        assertEquals(EstadoPlan.FINALIZADO, cerrado.getEstado());
        assertDoesNotThrow(this::crearPlan, "el lote debe poder sembrarse otra vez");
        assertEquals(2, service.listarPorLote(LOTE).size());
    }

    @Test
    void cancelarUnPlanLiberaElLote() {
        PlanResponse plan = crearPlan();

        PlanResponse cancelado = service.actualizarEstado(plan.getId(),
                PlanPatchRequest.builder().estado(EstadoPlan.CANCELADO).build());

        assertEquals(EstadoPlan.CANCELADO, cancelado.getEstado());
        assertDoesNotThrow(this::crearPlan);
    }

    @Test
    void elEstadoDeUnPlanSoloSePuedeCambiarACancelado() {
        PlanResponse plan = crearPlan();

        assertThrows(ReglaNegocioException.class, () -> service.actualizarEstado(plan.getId(),
                PlanPatchRequest.builder().estado(EstadoPlan.FINALIZADO).build()));
    }

    // ---------- labores: RN-LAB-03 ----------

    @Test
    void noSePuedeSaltearUnaLabor() {
        PlanResponse plan = crearPlan();

        assertThrows(ReglaNegocioException.class, () -> cambiarLabor(plan, 2, EstadoLabor.EN_EJECUCION, null));
    }

    @Test
    void noSePuedeCompletarUnaLaborQueNoSeInicio() {
        PlanResponse plan = crearPlan();

        assertThrows(ReglaNegocioException.class, () -> cambiarLabor(plan, 1, EstadoLabor.COMPLETADA, null));
    }

    @Test
    void unaLaborNoVuelveAPendiente() {
        PlanResponse plan = crearPlan();
        cambiarLabor(plan, 1, EstadoLabor.EN_EJECUCION, null);

        assertThrows(ReglaNegocioException.class, () -> cambiarLabor(plan, 1, EstadoLabor.PENDIENTE, null));
    }

    // ---------- fumigación: RN-LAB-05 ----------

    private PlanResponse planListoParaFumigar() {
        PlanResponse plan = crearPlan();
        hacerLabor(plan, 1);
        hacerLabor(plan, 2);
        return plan;
    }

    @Test
    void fumigaConClimaFavorableYRegistraElTratamiento() {
        PlanResponse plan = planListoParaFumigar();

        PlanResponse resultado = cambiarLabor(plan, 3, EstadoLabor.EN_EJECUCION, TRATAMIENTO);

        LaborResponse fumigacion = resultado.getLabores().get(2);
        assertEquals(EstadoLabor.EN_EJECUCION, fumigacion.getEstado());
        assertEquals(TRATAMIENTO, fumigacion.getTratamientoId());
        assertTrue(resultado.getAdvertencias() == null || resultado.getAdvertencias().isEmpty());
        assertEquals(1, clima.llamadas);
    }

    @Test
    void noFumigaConVientoFuerteYLaLaborQuedaPendiente() {
        PlanResponse plan = planListoParaFumigar();
        clima.respuesta = pronostico(30.0, 0.0);

        assertThrows(ReglaNegocioException.class, () -> cambiarLabor(plan, 3, EstadoLabor.EN_EJECUCION, TRATAMIENTO));

        PlanResponse actual = service.obtenerPorId(plan.getId());
        assertEquals(EstadoLabor.PENDIENTE, actual.getLabores().get(2).getEstado());
    }

    @Test
    void noFumigaSiSePrevenLluvias() {
        PlanResponse plan = planListoParaFumigar();
        clima.respuesta = pronostico(5.0, 2.0);

        assertThrows(ReglaNegocioException.class, () -> cambiarLabor(plan, 3, EstadoLabor.EN_EJECUCION, TRATAMIENTO));
    }

    @Test
    void fumigarExigeIndicarElTratamiento() {
        PlanResponse plan = planListoParaFumigar();

        assertThrows(ReglaNegocioException.class, () -> cambiarLabor(plan, 3, EstadoLabor.EN_EJECUCION, null));
        assertEquals(0, clima.llamadas);
    }

    @Test
    void elTratamientoDebeSerDelCultivoDelPlan() {
        PlanResponse plan = planListoParaFumigar();
        agronomia.tratamientos.put(8L, new TratamientoInfo(8L, 99L, "Otro", 15.0, 4));

        assertThrows(ReglaNegocioException.class, () -> cambiarLabor(plan, 3, EstadoLabor.EN_EJECUCION, 8L));
    }

    @Test
    void siElProveedorDeClimaFallaFumigaConAdvertencia() {
        PlanResponse plan = planListoParaFumigar();
        clima.caido = true;

        PlanResponse resultado = cambiarLabor(plan, 3, EstadoLabor.EN_EJECUCION, TRATAMIENTO);

        assertEquals(EstadoLabor.EN_EJECUCION, resultado.getLabores().get(2).getEstado());
        assertEquals(1, resultado.getAdvertencias().size());
        assertTrue(resultado.getAdvertencias().get(0).contains("Open-Meteo"));
    }

    @Test
    void siElLoteNoTieneCoordenadasFumigaConAdvertenciaSinConsultarElClima() {
        PlanResponse plan = planListoParaFumigar();
        campos.lotes.put(LOTE, new LoteInfo(LOTE, CAMPO, 1, null, null, "TIPO_2"));

        PlanResponse resultado = cambiarLabor(plan, 3, EstadoLabor.EN_EJECUCION, TRATAMIENTO);

        assertEquals(1, resultado.getAdvertencias().size());
        assertEquals(0, clima.llamadas);
    }

    @Test
    void unaFumigacionFueraDeSecuenciaNoConsultaElClima() {
        PlanResponse plan = crearPlan();

        assertThrows(ReglaNegocioException.class, () -> cambiarLabor(plan, 3, EstadoLabor.EN_EJECUCION, TRATAMIENTO));
        assertEquals(0, clima.llamadas);
    }
}
