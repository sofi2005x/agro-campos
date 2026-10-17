package clase.spring.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

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
    @Column(nullable = false, unique = true, length = 100)
    private String nombre; // Ej: "Oruga Militar", "Roya Asiática", "Yuyo Colorado"

    @Column(length = 255)
    private String descripcion;
}