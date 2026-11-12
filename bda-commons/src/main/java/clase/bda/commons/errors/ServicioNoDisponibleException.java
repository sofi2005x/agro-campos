package clase.bda.commons.errors;

/** Otro microservicio o una API externa no respondió. La API responde 503. */
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
