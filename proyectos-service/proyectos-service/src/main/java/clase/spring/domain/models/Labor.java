package clase.spring.domain.models;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Un laboreo de un plan de siembra. Se crea copiando la secuencia de labores del cultivo, por lo que
 * los cambios posteriores en el catálogo no afectan a planes existentes.
 * Sus transiciones las gobierna el plan (agregado raíz): por eso los métodos no son públicos.
 */
@Getter
@Entity
@Table(name = "labores")
public class Labor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanDeSiembra plan;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoLabor tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoLabor estado;

    // Solo para fumigaciones: tratamiento aplicado (referencia a agronomia-service)
    private Long tratamientoId;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    protected Labor() {
        // requerido por JPA
    }

    Labor(PlanDeSiembra plan, int orden, String nombre) {
        this.plan = plan;
        this.orden = orden;
        this.nombre = nombre;
        this.tipo = TipoLabor.desde(nombre);
        this.estado = EstadoLabor.PENDIENTE;
    }

    void iniciar(Long tratamientoId, LocalDateTime ahora) {
        this.estado = EstadoLabor.EN_EJECUCION;
        this.tratamientoId = tratamientoId;
        this.fechaInicio = ahora;
    }

    void completar(LocalDateTime ahora) {
        this.estado = EstadoLabor.COMPLETADA;
        this.fechaFin = ahora;
    }

}
