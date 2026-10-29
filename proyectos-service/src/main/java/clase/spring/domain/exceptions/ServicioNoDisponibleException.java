package clase.spring.domain.exceptions;

/**
 * Otro microservicio (o la API externa de clima) no respondió o devolvió una respuesta inesperada.
 */
public class ServicioNoDisponibleException extends RuntimeException {

    private final String servicio;

    public ServicioNoDisponibleException(String servicio, Throwable causa) {
        super(servicio + " no disponible", causa);
        this.servicio = servicio;
    }

    public ServicioNoDisponibleException(String servicio, String detalle) {
        super(servicio + " no disponible: " + detalle);
        this.servicio = servicio;
    }

    public String getServicio() {
        return servicio;
    }
}
