package clase.spring.domain.models;

import clase.spring.domain.exceptions.LaborNoEncontradaException;
import clase.spring.domain.exceptions.ReglaNegocioException;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad rica y raíz del agregado: un cultivo sembrado en un lote, con sus labores.
 * No tiene setters: el estado solo cambia mediante comportamiento que respeta las reglas.
 * Referencia a campo, lote y cultivo por ID, porque esos datos pertenecen a otros microservicios.
 */
@Getter
@Entity
@Table(name = "planes_de_siembra")
public class PlanDeSiembra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long campoId;

    @Column(nullable = false)
    private Long loteId;

    @Column(nullable = false)
    private Long cultivoId;

    // Copia del nombre del cultivo al momento de crear el plan (evita consultar al catálogo para listar)
    @Column(nullable = false, length = 80)
    private String cultivoNombre;

    @Column(nullable = false)
    private LocalDate fechaSiembraPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPlan estado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<Labor> labores = new ArrayList<>();

    protected PlanDeSiembra() {
        // requerido por JPA
    }

    /**
     * Crea un plan PLANIFICADO. Las labores se generan, en orden, a partir de la secuencia del cultivo.
     */
    public static PlanDeSiembra crear(Long campoId, Long loteId, Long cultivoId, String cultivoNombre,
                                      LocalDate fechaSiembraPrevista, List<String> laboreos) {
        if (laboreos == null || laboreos.isEmpty()) {
            throw new ReglaNegocioException("El cultivo " + cultivoNombre + " no tiene laboreos definidos: "
                    + "no se puede crear un plan de siembra");
        }
        PlanDeSiembra plan = new PlanDeSiembra();
        plan.campoId = Objects.requireNonNull(campoId, "campoId");
        plan.loteId = Objects.requireNonNull(loteId, "loteId");
        plan.cultivoId = Objects.requireNonNull(cultivoId, "cultivoId");
        plan.cultivoNombre = cultivoNombre;
        plan.fechaSiembraPrevista = Objects.requireNonNull(fechaSiembraPrevista, "fechaSiembraPrevista");
        plan.estado = EstadoPlan.PLANIFICADO;
        plan.fechaCreacion = LocalDateTime.now();
        int orden = 1;
        for (String nombre : laboreos) {
            if (nombre == null || nombre.isBlank()) {
                throw new ReglaNegocioException("El cultivo " + cultivoNombre + " tiene un laboreo sin nombre");
            }
            plan.labores.add(new Labor(plan, orden++, nombre.trim()));
        }
        return plan;
    }

    public Labor buscarLabor(Long laborId) {
        return labores.stream()
                .filter(l -> l.getId() != null && l.getId().equals(laborId))
                .findFirst()
                .orElseThrow(() -> new LaborNoEncontradaException(id, laborId));
    }

    /**
     * RN-LAB-03: valida que la labor pueda iniciarse (plan activo, labor pendiente y todas las anteriores
     * completadas, en el orden definido por el cultivo). Es público para que el servicio de aplicación pueda
     * hacer este chequeo antes de consultar el clima (RN-LAB-05) y no consultar si la labor no corresponde.
     */
    public void validarPuedeIniciar(Labor labor) {
        validarPertenece(labor);
        validarActivo();
        if (labor.getEstado() != EstadoLabor.PENDIENTE) {
            throw new ReglaNegocioException("La labor '" + labor.getNombre() + "' ya está " + labor.getEstado());
        }
        for (Labor anterior : labores) {
            if (anterior.getOrden() < labor.getOrden() && anterior.getEstado() != EstadoLabor.COMPLETADA) {
                throw new ReglaNegocioException("No se puede iniciar '" + labor.getNombre() + "': primero debe completarse '"
                        + anterior.getNombre() + "' (labor " + anterior.getOrden() + ")");
            }
        }
    }

    /**
     * Inicia la labor. La primera labor iniciada pone el plan EN_CURSO.
     * El tratamiento solo se registra en las fumigaciones.
     */
    public void iniciarLabor(Labor labor, Long tratamientoId) {
        validarPuedeIniciar(labor);
        Long tratamiento = labor.getTipo() == TipoLabor.FUMIGAR ? tratamientoId : null;
        labor.iniciar(tratamiento, LocalDateTime.now());
        if (estado == EstadoPlan.PLANIFICADO) {
            estado = EstadoPlan.EN_CURSO;
        }
    }

    /**
     * Completa la labor. RN-LAB-04: al completarse la COSECHA el plan queda FINALIZADO, y como deja de estar
     * activo el lote queda libre para la próxima siembra (RN-LAB-02).
     */
    public void completarLabor(Labor labor) {
        validarPertenece(labor);
        validarActivo();
        if (labor.getEstado() != EstadoLabor.EN_EJECUCION) {
            throw new ReglaNegocioException("La labor '" + labor.getNombre() + "' debe estar EN_EJECUCION para completarse "
                    + "(estado actual: " + labor.getEstado() + ")");
        }
        labor.completar(LocalDateTime.now());
        if (labor.getTipo() == TipoLabor.COSECHAR) {
            estado = EstadoPlan.FINALIZADO;
        }
    }

    /** Un plan activo puede cancelarse en cualquier momento (incendio, sequía, cultivo que no nace...). */
    public void cancelar() {
        if (!estado.esActivo()) {
            throw new ReglaNegocioException("Un plan " + estado + " no puede cancelarse");
        }
        estado = EstadoPlan.CANCELADO;
    }

    private void validarActivo() {
        if (!estado.esActivo()) {
            throw new ReglaNegocioException("El plan está " + estado + ": no admite cambios en sus labores");
        }
    }

    private void validarPertenece(Labor labor) {
        if (labor == null || !labores.contains(labor)) {
            throw new IllegalArgumentException("La labor no pertenece a este plan");
        }
    }

}
