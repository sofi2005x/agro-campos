package clase.spring.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cultivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cultivo es obligatorio")
    @Column(nullable = false, unique = true, length = 80)
    private String nombre; // Ej: Soja, Maíz, Trigo, Girasol

    // Suelos donde este cultivo puede sembrarse
    @ElementCollection(targetClass = TipoSuelo.class)
    @CollectionTable(name = "cultivo_suelos_aptos", joinColumns = @JoinColumn(name = "cultivo_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_suelo")
    @Builder.Default
    private Set<TipoSuelo> suelosAptos = new HashSet<>();

    // Secuencia ordenada de labores por cultivo
    @ElementCollection
    @CollectionTable(name = "cultivo_labores", joinColumns = @JoinColumn(name = "cultivo_id"))
    @OrderColumn(name = "orden")
    @Column(name = "labor")
    @Builder.Default
    private List<String> labores = new ArrayList<>();

    /**
     * Regla de negocio: verifica si este cultivo puede sembrarse en un determinado suelo
     */
    public boolean esAptoParaSuelo(TipoSuelo suelo) {
        return this.suelosAptos != null && this.suelosAptos.contains(suelo);
    }
}