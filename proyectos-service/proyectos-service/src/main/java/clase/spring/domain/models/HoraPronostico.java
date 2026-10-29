package clase.spring.domain.models;

import java.time.LocalDateTime;

/** Pronóstico de una hora: viento en km/h y precipitación en mm (hora en UTC). */
public record HoraPronostico(LocalDateTime hora, double vientoKmH, double precipitacionMm) {
}
