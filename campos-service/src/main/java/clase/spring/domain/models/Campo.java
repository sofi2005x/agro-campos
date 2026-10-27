package clase.spring.domain.models;

import clase.spring.domain.models.Lote;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Campo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del campo es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Positive(message = "La superficie debe ser mayor a 0")
    @Column(nullable = false)
    private Double superficieTotalHas;

    @OneToMany(mappedBy = "campo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Lote> lotes = new ArrayList<>();
}
