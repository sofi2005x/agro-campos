package clase.spring.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "plagas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Plaga {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la plaga es obligatorio")
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @NotBlank(message = "La descripcion de la plaga es obligatoria")
    @Column(nullable = false, length = 100)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPlaga tipo;

}