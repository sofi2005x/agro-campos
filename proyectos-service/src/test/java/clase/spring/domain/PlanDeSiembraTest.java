package clase.spring.domain;

import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.models.EstadoLabor;
import clase.spring.domain.models.EstadoPlan;
import clase.spring.domain.models.Labor;
import clase.spring.domain.models.PlanDeSiembra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanDeSiembraTest {

    private PlanDeSiembra plan;

    @BeforeEach
    void setUp() {
        plan = PlanDeSiembra.crear(1L, 10L, 5L, "Soja", LocalDate.now().plusDays(7),
                List.of("Arar", "Sembrar", "Fumigar", "Cosechar"));
    }

    private Labor labor(int orden) {
        return plan.getLabores().get(orden - 1);
    }

    @Test
    void crearGeneraLasLaboresEnElOrdenDelCultivo() {
        assertEquals(EstadoPlan.PLANIFICADO, plan.getEstado());
        assertEquals(4, plan.getLabores().size());
        assertEquals("Arar", labor(1).getNombre());
        assertEquals(1, labor(1).getOrden());
        assertEquals("Cosechar", labor(4).getNombre());
        assertTrue(plan.getLabores().stream().allMatch(l -> l.getEstado() == EstadoLabor.PENDIENTE));
    }

    @Test
    void crearConCultivoSinLaboresFalla() {
        assertThrows(ReglaNegocioException.class, () ->
                PlanDeSiembra.crear(1L, 10L, 5L, "Soja", LocalDate.now(), List.of()));
    }

    // ---- RN-LAB-03: secuencia estricta ----

    @Test
    void noSePuedeSaltearUnaLabor() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> plan.iniciarLabor(labor(2), null));
        assertTrue(ex.getMessage().contains("Arar"));
    }

    @Test
    void noSePuedeIniciarUnaLaborSiLaAnteriorSoloEstaEnEjecucion() {
        plan.iniciarLabor(labor(1), null);
        assertThrows(ReglaNegocioException.class, () -> plan.iniciarLabor(labor(2), null));
    }

    @Test
    void iniciarLaPrimeraLaborPoneElPlanEnCurso() {
        plan.iniciarLabor(labor(1), null);
        assertEquals(EstadoPlan.EN_CURSO, plan.getEstado());
        assertEquals(EstadoLabor.EN_EJECUCION, labor(1).getEstado());
        assertNotNull(labor(1).getFechaInicio());
    }

    @Test
    void noSePuedeCompletarUnaLaborQueNoEstaEnEjecucion() {
        assertThrows(ReglaNegocioException.class, () -> plan.completarLabor(labor(1)));
    }

    @Test
    void noSePuedeIniciarDosVecesLaMismaLabor() {
        plan.iniciarLabor(labor(1), null);
        assertThrows(ReglaNegocioException.class, () -> plan.iniciarLabor(labor(1), null));
    }

    // ---- RN-LAB-04: cierre con cosecha ----

    @Test
    void completarLaCosechaFinalizaElPlan() {
        recorrerHastaLaCosecha();
        plan.iniciarLabor(labor(4), null);
        assertEquals(EstadoPlan.EN_CURSO, plan.getEstado());

        plan.completarLabor(labor(4));

        assertEquals(EstadoPlan.FINALIZADO, plan.getEstado());
        assertFalse(plan.getEstado().esActivo(), "al finalizar el plan el lote queda libre");
    }

    @Test
    void completarLaboresIntermediasNoFinalizaElPlan() {
        plan.iniciarLabor(labor(1), null);
        plan.completarLabor(labor(1));
        assertEquals(EstadoPlan.EN_CURSO, plan.getEstado());
    }

    @Test
    void unPlanFinalizadoNoAdmiteMasCambios() {
        recorrerHastaLaCosecha();
        plan.iniciarLabor(labor(4), null);
        plan.completarLabor(labor(4));
        assertThrows(ReglaNegocioException.class, plan::cancelar);
    }

    // ---- tratamiento solo en fumigaciones ----

    @Test
    void elTratamientoSoloSeRegistraEnFumigaciones() {
        plan.iniciarLabor(labor(1), 99L);
        assertNull(labor(1).getTratamientoId());

        plan.completarLabor(labor(1));
        plan.iniciarLabor(labor(2), null);
        plan.completarLabor(labor(2));
        plan.iniciarLabor(labor(3), 7L);
        assertEquals(7L, labor(3).getTratamientoId());
    }

    // ---- cancelación ----

    @Test
    void sePuedeCancelarUnPlanActivoYLiberaElLote() {
        plan.iniciarLabor(labor(1), null);
        plan.cancelar();
        assertEquals(EstadoPlan.CANCELADO, plan.getEstado());
        assertFalse(plan.getEstado().esActivo());
    }

    @Test
    void unPlanCanceladoNoAdmiteIniciarLabores() {
        plan.cancelar();
        assertThrows(ReglaNegocioException.class, () -> plan.iniciarLabor(labor(1), null));
    }

    @Test
    void noSePuedeCancelarDosVeces() {
        plan.cancelar();
        assertThrows(ReglaNegocioException.class, plan::cancelar);
    }

    private void recorrerHastaLaCosecha() {
        for (int orden = 1; orden <= 3; orden++) {
            plan.iniciarLabor(labor(orden), 7L);
            plan.completarLabor(labor(orden));
        }
    }
}
