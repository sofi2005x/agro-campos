package clase.spring.infrastructure.adapters;

import clase.spring.domain.exceptions.ReferenciaNoEncontradaException;
import clase.spring.domain.exceptions.ServicioNoDisponibleException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Supplier;

/**
 * Traduce los errores de RestClient a las excepciones del dominio:
 * 404 -> ReferenciaNoEncontradaException (si se informó un mensaje); cualquier otro error HTTP,
 * de conexión o de lectura -> ServicioNoDisponibleException.
 */
final class LlamadaRemota {

    private LlamadaRemota() {
    }

    static <T> T ejecutar(String servicio, String mensajeNoEncontrado, Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404 && mensajeNoEncontrado != null) {
                throw new ReferenciaNoEncontradaException(mensajeNoEncontrado);
            }
            throw new ServicioNoDisponibleException(servicio, ex);
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException(servicio, ex);
        }
    }
}
