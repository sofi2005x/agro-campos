package clase.spring.domain.models;

import clase.spring.domain.exceptions.ReglaNegocioException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "tratamientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plaga_id", nullable = false)
    private Plaga plaga;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agroquimico_id", nullable = false)
    private Agroquimico agroquimico;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    @Positive(message = "La dosis debe ser mayor a 0")
    @Column(nullable = false)
    private Double dosisPorHectarea; // Ej: 2.5

    @NotBlank
    @Column(nullable = false, length = 20)
    private String unidadMedida; // Ej: "Litros/ha", "Kg/ha"

    public static Tratamiento crear(Plaga plaga, Agroquimico agroquimico, Cultivo cultivo,
                                    Double dosisPorHectarea, String unidadMedida) {
        validar(dosisPorHectarea);
        validarCoherencia(plaga, agroquimico);
        return Tratamiento.builder()
                .activo(true)
                .plaga(plaga)
                .agroquimico(agroquimico)
                .cultivo(cultivo)
                .dosisPorHectarea(dosisPorHectarea)
                .unidadMedida(unidadMedida)
                .build();
    }

    public void actualizar(Plaga plaga, Agroquimico agroquimico, Cultivo cultivo,
                           Double dosisPorHectarea, String unidadMedida) {
        validar(dosisPorHectarea);
        validarCoherencia(plaga, agroquimico);
        this.plaga = plaga;
        this.agroquimico = agroquimico;
        this.cultivo = cultivo;
        this.dosisPorHectarea = dosisPorHectarea;
        this.unidadMedida = unidadMedida;
    }

    // RN-TRA-02: la dosis por hectárea debe ser estrictamente positiva
    private static void validar(Double dosisPorHectarea) {
        if (dosisPorHectarea == null || dosisPorHectarea <= 0) {
            throw new ReglaNegocioException("La dosis por hectárea debe ser estrictamente positiva (> 0)");
        }
    }

    // RN-TRA-04: coherencia entre el tipo de plaga y el tipo de agroquímico
    private static void validarCoherencia(Plaga plaga, Agroquimico agroquimico) {
        TipoPlaga tipoPlaga = plaga.getTipoPlaga();
        TipoAgroquimico tipoAgro = agroquimico.getTipo();

        if (tipoAgro == TipoAgroquimico.FERTILIZANTE) {
            throw new ReglaNegocioException("Un fertilizante no combate ninguna plaga");
        }
        if (!tipoPlaga.admite(tipoAgro)) {
            throw new ReglaNegocioException(
                    "Para plagas de tipo " + tipoPlaga + " debe utilizarse un " + tipoPlaga.getTipoQueCombate());
        }
    }
}