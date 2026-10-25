package clase.spring.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cultivos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cultivo es obligatorio")
    @Column(nullable = false, unique = true, length = 80)
    private String nombre; // Ej: Soja, Maíz, Trigo, Girasol

    // Suelos donde este cultivo puede sembrarse (ej: Soja es apta para TIPO_1 y TIPO_2)
    @ElementCollection(targetClass = TipoSuelo.class)
    @CollectionTable(name = "cultivo_suelos_aptos", joinColumns = @JoinColumn(name = "cultivo_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_suelo")
    @Builder.Default
    private Set<TipoSuelo> suelosAptos = new HashSet<>();

    /**
     * Regla de negocio: verifica si este cultivo puede sembrarse en un determinado suelo
     */
    public boolean esAptoParaSuelo(TipoSuelo suelo) {
        return this.suelosAptos != null && this.suelosAptos.contains(suelo);
    }
}