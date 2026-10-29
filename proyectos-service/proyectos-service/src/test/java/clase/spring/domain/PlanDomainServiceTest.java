package clase.spring.domain;

import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.models.*;
import clase.spring.domain.services.PlanDomainService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlanDomainServiceTest {

    private final PlanDomainService service = new PlanDomainService();

    // ---- RN-LAB-01: aptitud de suelo ----

    private final CultivoInfo soja = new CultivoInfo(5L, "Soja", Set.of("TIPO_1", "TIPO_2"), List.of("Arar"));

    @Test
    void sePermiteSembrarEnUnSueloApto() {
        LoteInfo lote = new LoteInfo(10L, 1L, 1, -31.4, -64.1, "TIPO_2");
        assertDoesNotThrow(() -> service.validarAptitudSuelo(lote, soja));
    }

    @Test
    void noSePermiteSembrarEnUnSueloNoApto() {
        LoteInfo lote = new LoteInfo(10L, 1L, 1, -31.4, -64.1, "TIPO_5");
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.validarAptitudSuelo(lote, soja));
        assertTrue(ex.getMessage().contains("TIPO_5"));
    }

    // ---- RN-LAB-02: exclusividad del lote ----

    private PlanDeSiembra plan(Long id, boolean finalizar, boolean cancelar) throws Exception {
        PlanDeSiembra plan = PlanDeSiembra.crear(1L, 10L, 5L, "Soja", LocalDate.now(), List.of("Cosechar"));
        Field f = PlanDeSiembra.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(plan, id);
        if (finalizar) {
            plan.iniciarLabor(plan.getLabores().get(0), null);
            plan.completarLabor(plan.getLabores().get(0));
        }
        if (cancelar) {
            plan.cancelar();
        }
        return plan;
    }

    @Test
    void loteSinPlanesEstaDisponible() {
        assertDoesNotThrow(() -> service.validarLoteDisponible(10L, List.of()));
    }

    @Test
    void loteConPlanPlanificadoNoEstaDisponible() throws Exception {
        List<PlanDeSiembra> planes = List.of(plan(1L, false, false));
        assertThrows(ReglaNegocioException.class, () -> service.validarLoteDisponible(10L, planes));
    }

    @Test
    void loteConPlanEnCursoNoEstaDisponible() throws Exception {
        PlanDeSiembra enCurso = PlanDeSiembra.crear(1L, 10L, 5L, "Soja", LocalDate.now(), List.of("Arar", "Cosechar"));
        enCurso.iniciarLabor(enCurso.getLabores().get(0), null);
        assertEquals(EstadoPlan.EN_CURSO, enCurso.getEstado());
        assertThrows(ReglaNegocioException.class, () -> service.validarLoteDisponible(10L, List.of(enCurso)));
    }

    @Test
    void loteConPlanesFinalizadosOCanceladosEstaDisponible() throws Exception {
        List<PlanDeSiembra> historico = List.of(plan(1L, true, false), plan(2L, false, true));
        assertEquals(EstadoPlan.FINALIZADO, historico.get(0).getEstado());
        assertEquals(EstadoPlan.CANCELADO, historico.get(1).getEstado());
        assertDoesNotThrow(() -> service.validarLoteDisponible(10L, historico));
    }

    // ---- RN-LAB-05: condiciones climáticas ----

    private final TratamientoInfo tratamiento = new TratamientoInfo(3L, 5L, "Coragen", 15.0, 4);

    private PronosticoClima pronostico(double[] viento, double[] lluvia) {
        LocalDateTime base = LocalDateTime.of(2026, 10, 26, 14, 0);
        List<HoraPronostico> horas = new java.util.ArrayList<>();
        for (int i = 0; i < viento.length; i++) {
            horas.add(new HoraPronostico(base.plusHours(i), viento[i], lluvia[i]));
        }
        return new PronosticoClima(horas);
    }

    @Test
    void sePuedeFumigarConVientoSuaveYSinLluvia() {
        PronosticoClima p = pronostico(new double[]{8, 10, 12, 9, 7}, new double[]{0, 0, 0, 0, 0});
        assertDoesNotThrow(() -> service.validarCondicionesFumigacion(tratamiento, p));
    }

    @Test
    void sePuedeFumigarConVientoEnElLimite() {
        PronosticoClima p = pronostico(new double[]{15, 5}, new double[]{0, 0});
        assertDoesNotThrow(() -> service.validarCondicionesFumigacion(tratamiento, p));
    }

    @Test
    void noSePuedeFumigarConVientoSuperiorAlMaximo() {
        PronosticoClima p = pronostico(new double[]{28, 10}, new double[]{0, 0});
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.validarCondicionesFumigacion(tratamiento, p));
        assertTrue(ex.getMessage().contains("28 km/h"));
        assertTrue(ex.getMessage().contains("15 km/h"));
    }

    @Test
    void elVientoSoloSeEvaluaEnLaHoraDeAplicacion() {
        // viento fuerte recién dentro de 3 horas: no afecta a la aplicación
        PronosticoClima p = pronostico(new double[]{8, 9, 10, 40}, new double[]{0, 0, 0, 0});
        assertDoesNotThrow(() -> service.validarCondicionesFumigacion(tratamiento, p));
    }

    @Test
    void noSePuedeFumigarSiSePrevenLluviaDentroDeLasHorasLibres() {
        PronosticoClima p = pronostico(new double[]{8, 9, 10, 9, 8}, new double[]{0, 0, 0, 1.2, 0});
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.validarCondicionesFumigacion(tratamiento, p));
        assertTrue(ex.getMessage().contains("lluvia"));
        assertTrue(ex.getMessage().contains("17:00"));
    }

    @Test
    void unaLloviznaDespreciableNoBloquea() {
        PronosticoClima p = pronostico(new double[]{8, 9}, new double[]{0.05, 0});
        assertDoesNotThrow(() -> service.validarCondicionesFumigacion(tratamiento, p));
    }

    @Test
    void sinLimitesInformadosSeUsanLosValoresPorDefecto() {
        TratamientoInfo sinLimites = new TratamientoInfo(3L, 5L, "Coragen", null, null);
        assertEquals(15.0, sinLimites.vientoMaximoKmH());
        assertEquals(4, sinLimites.horasLibresLluvia());
    }

    @Test
    void elTratamientoDebeCorresponderAlCultivoDelPlan() {
        assertDoesNotThrow(() -> service.validarTratamientoDelCultivo(tratamiento, 5L));
        assertThrows(ReglaNegocioException.class, () -> service.validarTratamientoDelCultivo(tratamiento, 6L));
    }
}
