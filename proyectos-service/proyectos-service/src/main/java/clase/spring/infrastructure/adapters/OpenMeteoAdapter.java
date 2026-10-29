package clase.spring.infrastructure.adapters;

import clase.spring.domain.exceptions.ServicioNoDisponibleException;
import clase.spring.domain.models.HoraPronostico;
import clase.spring.domain.models.PronosticoClima;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import clase.spring.domain.ports.ClimaPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter de {@link ClimaPort} para Open-Meteo (https://open-meteo.com), API pública de pronóstico que no requiere API key.
 * Pide el pronóstico horario de viento (km/h) y precipitación (mm) en UTC, y devuelve la ventana que va
 * desde la hora actual hasta {@code horas} horas después.
 */
@Component
public class OpenMeteoAdapter implements ClimaPort {

    private static final String SERVICIO = "Open-Meteo";

    private final RestClient restClient;

    public OpenMeteoAdapter(@Qualifier("climaRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public PronosticoClima obtenerPronostico(double latitud, double longitud, int horas) {
        PronosticoRemoto respuesta = LlamadaRemota.ejecutar(SERVICIO, null,
                () -> restClient.get()
                        .uri(uri -> uri.path("/v1/forecast")
                                .queryParam("latitude", latitud)
                                .queryParam("longitude", longitud)
                                .queryParam("hourly", "wind_speed_10m,precipitation")
                                .queryParam("wind_speed_unit", "kmh")
                                .queryParam("timezone", "UTC")
                                .queryParam("forecast_days", 3)
                                .build())
                        .retrieve()
                        .body(PronosticoRemoto.class));

        if (respuesta == null || respuesta.hourly() == null || respuesta.hourly().time() == null) {
            throw new ServicioNoDisponibleException(SERVICIO, "respuesta inesperada");
        }

        Horario horario = respuesta.hourly();
        LocalDateTime desde = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
        LocalDateTime hasta = desde.plusHours(horas);

        List<HoraPronostico> ventana = new ArrayList<>();
        for (int i = 0; i < horario.time().size(); i++) {
            LocalDateTime hora = LocalDateTime.parse(horario.time().get(i));
            Double viento = valor(horario.viento(), i);
            Double lluvia = valor(horario.precipitacion(), i);
            if (hora.isBefore(desde) || hora.isAfter(hasta) || viento == null || lluvia == null) {
                continue;
            }
            ventana.add(new HoraPronostico(hora, viento, lluvia));
        }

        if (ventana.isEmpty()) {
            throw new ServicioNoDisponibleException(SERVICIO, "sin datos para el período solicitado");
        }
        return new PronosticoClima(ventana);
    }

    private static Double valor(List<Double> serie, int indice) {
        return serie != null && indice < serie.size() ? serie.get(indice) : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PronosticoRemoto(Horario hourly) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Horario(List<String> time,
                           @JsonProperty("wind_speed_10m") List<Double> viento,
                           @JsonProperty("precipitation") List<Double> precipitacion) {
    }
}
