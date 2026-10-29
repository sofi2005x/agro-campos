package clase.spring.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "agroquimicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agroquimico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @NotBlank(message = "El nombre comercial es obligatorio")
    @Column(nullable = false, unique = true, length = 100)
    private String nombreComercial; // Ej: "Glifosato Roundup", "Coragen"

    @NotBlank(message = "El principio activo es obligatorio")
    @Column(nullable = false, length = 100)
    private String principioActivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAgroquimico tipo;

    // Regla agronómica: Viento máximo en km/h antes de que el viento desplace la gota (deriva)
    @Positive
    @Builder.Default
    private Double vientoMaximoPermitidoKmH = 15.0;

    // Regla agronómica: Horas que debe pasar sin lluvia para que la planta absorba el químico
    @Positive
    @Builder.Default
    private Integer horasLibresDeLluviaRequeridas = 4;
}
