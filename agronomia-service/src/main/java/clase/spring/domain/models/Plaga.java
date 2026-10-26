package clase.spring.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "plagas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la plaga es obligatorio")
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPlaga tipoPlaga;
}
