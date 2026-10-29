package clase.spring.domain.models;

import java.util.List;

/**
 * Ventana de pronóstico horario que comienza en la hora de aplicación.
 * La primera hora es el momento en que se aplicaría el producto.
 */
public record PronosticoClima(List<HoraPronostico> horas) {

    public PronosticoClima {
        horas = horas == null ? List.of() : List.copyOf(horas);
    }
}
