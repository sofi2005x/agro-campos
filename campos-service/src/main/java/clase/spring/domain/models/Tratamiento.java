package clase.spring.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "tratamientos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del tratamiento es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plaga_id", nullable = false)
    private Plaga plaga;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agroquimico_id", nullable = false)
    private Agroquimico agroquimico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    @Positive(message = "La dosis debe ser mayor a 0")
    @Column(nullable = false)
    private Double dosisPorHectarea; // Ej: 2.5

    @Column(nullable = false, length = 20)
    private String unidadMedida; // Ej: "Litros/ha", "Kg/ha"
}