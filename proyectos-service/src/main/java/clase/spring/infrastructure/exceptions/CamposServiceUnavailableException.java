package clase.spring.infrastructure.exceptions;

public class CamposServiceUnavailableException extends RuntimeException {

    public CamposServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
