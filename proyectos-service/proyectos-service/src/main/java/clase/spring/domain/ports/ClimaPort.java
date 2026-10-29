package clase.spring.domain.ports;

import clase.spring.domain.models.PronosticoClima;

/** Pronóstico meteorológico para unas coordenadas. */
public interface ClimaPort {

    /**
     * @param horas cantidad de horas, a partir de la actual, que debe cubrir el pronóstico
     * @return ventana horaria cuya primera hora es la de aplicación
     * @throws clase.spring.domain.exceptions.ServicioNoDisponibleException si el proveedor no responde
     *                                                                      o devuelve datos inutilizables
     */
    PronosticoClima obtenerPronostico(double latitud, double longitud, int horas);
}
