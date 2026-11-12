package clase.bda.commons.errors;

/** Un recurso pedido no existe. La API responde 404. Los servicios extienden esta clase (CampoNoEncontrado...). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
