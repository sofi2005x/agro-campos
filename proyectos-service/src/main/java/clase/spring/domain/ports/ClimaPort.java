package clase.spring.domain.ports;

import clase.spring.domain.models.PronosticoClima;

/** Pronóstico meteorológico para unas coordenadas. */
public interface ClimaPort {

    PronosticoClima obtenerPronostico(double latitud, double longitud, int horas);
}
