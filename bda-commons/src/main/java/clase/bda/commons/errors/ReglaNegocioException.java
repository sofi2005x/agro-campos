package clase.bda.commons.errors;

/** Se violó una regla de negocio. La API responde 400. Los servicios pueden extenderla. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
