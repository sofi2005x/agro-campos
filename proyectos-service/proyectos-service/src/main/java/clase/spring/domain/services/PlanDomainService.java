package clase.spring.domain.services;

import clase.spring.domain.exceptions.ReglaNegocioException;
import clase.spring.domain.models.CultivoInfo;
import clase.spring.domain.models.HoraPronostico;
import clase.spring.domain.models.LoteInfo;
import clase.spring.domain.models.PlanDeSiembra;
import clase.spring.domain.models.PronosticoClima;
import clase.spring.domain.models.TratamientoInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Reglas que no pertenecen a una sola entidad. No accede a la base ni a otros servicios: el servicio de
 * aplicación obtiene los datos y este servicio toma la decisión.
 */
@Service
public class PlanDomainService {

    /** Precipitación horaria (mm) a partir de la cual se considera que llueve. */
    public static final double UMBRAL_PRECIPITACION_MM = 0.1;

    /**
     * RN-LAB-01 (Aptitud de suelo): un cultivo solo puede sembrarse en un lote cuyo tipo de suelo
     * pertenezca a la lista de suelos aptos del cultivo.
     */
    public void validarAptitudSuelo(LoteInfo lote, CultivoInfo cultivo) {
        if (!cultivo.esAptoParaSuelo(lote.tipoSuelo())) {
            throw new ReglaNegocioException(String.format(
                    "El cultivo %s no es apto para el suelo %s del lote %d (suelos aptos: %s)",
                    cultivo.nombre(), lote.tipoSuelo(), lote.id(), cultivo.suelosAptos()));
        }
    }

    /**
     * RN-LAB-02 (Exclusividad del lote): un lote solo puede tener un único plan activo a la vez
     * (PLANIFICADO o EN_CURSO). No se pueden solapar cultivos.
     */
    public void validarLoteDisponible(Long loteId, List<PlanDeSiembra> planesDelLote) {
        planesDelLote.stream()
                .filter(plan -> plan.getEstado().esActivo())
                .findFirst()
                .ifPresent(plan -> {
                    throw new ReglaNegocioException(String.format(
                            "El lote %d ya tiene un plan activo (plan %d, cultivo %s, estado %s)",
                            loteId, plan.getId(), plan.getCultivoNombre(), plan.getEstado()));
                });
    }

    /** El tratamiento informado en una fumigación debe corresponder al cultivo del plan. */
    public void validarTratamientoDelCultivo(TratamientoInfo tratamiento, Long cultivoId) {
        if (!cultivoId.equals(tratamiento.cultivoId())) {
            throw new ReglaNegocioException("El tratamiento " + tratamiento.id()
                    + " no corresponde al cultivo del plan");
        }
    }

    /**
     * RN-LAB-05 (Condiciones climáticas para fumigar): no se puede aplicar si el viento previsto en la hora de
     * aplicación supera el máximo del agroquímico (deriva), ni si se prevé lluvia dentro de las horas libres
     * de lluvia que el producto requiere (lavado). El pronóstico es la ventana que comienza en la hora de aplicación.
     */
    public void validarCondicionesFumigacion(TratamientoInfo tratamiento, PronosticoClima pronostico) {
        if (pronostico.horas().isEmpty()) {
            throw new IllegalArgumentException("El pronóstico no contiene horas");
        }

        HoraPronostico aplicacion = pronostico.horas().get(0);
        if (aplicacion.vientoKmH() > tratamiento.vientoMaximoKmH()) {
            throw new ReglaNegocioException(String.format(Locale.ROOT,
                    "No se puede fumigar: viento previsto de %.0f km/h supera el máximo de %.0f km/h de %s",
                    aplicacion.vientoKmH(), tratamiento.vientoMaximoKmH(), tratamiento.agroquimicoNombre()));
        }

        pronostico.horas().stream()
                .filter(hora -> hora.precipitacionMm() >= UMBRAL_PRECIPITACION_MM)
                .findFirst()
                .ifPresent(hora -> {
                    throw new ReglaNegocioException(String.format(Locale.ROOT,
                            "No se puede fumigar: se prevén %.1f mm de lluvia a las %s UTC, dentro de las %d horas "
                                    + "libres de lluvia que requiere %s",
                            hora.precipitacionMm(), hora.hora().toLocalTime(), tratamiento.horasLibresLluvia(),
                            tratamiento.agroquimicoNombre()));
                });
    }
}
