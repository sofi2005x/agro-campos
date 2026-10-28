package clase.spring.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "lotes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El número de lote es obligatorio")
    @Column(nullable = false)
    private Integer nroLote;

    @Positive(message = "Las hectáreas deben ser mayores a 0")
    @Column(nullable = false)
    private Double hectareas;

    // Coordenadas geográficas requeridas para la API externa de clima (Open-Meteo)
    private Double latitud;
    private Double longitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSuelo tipoSuelo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campo_id", nullable = false)
    @JsonIgnore
    private Campo campo;
}
